# Agent Flow Specification

**Version:** 1.0.0-draft  
**Status:** Proposal

---

## 1. Key Design Principles

The **Agent Flow** specification proposes the following key design principles

1. **Hybrid execution**
   * One process definition can target multiple platforms simultaneously; deterministic tasks on traditional engines, AI tasks on an AI platform, coordinated by a single orchestrator executing processes specified in Google ADK or LangGraph.
   * Orchestrator platform has awareness of target platforms and delegates node execution to those platforms based on execution bindings (described below)
   * Majority of logic is executed by target platforms when a task is invoked, however the orchestrator platform has ability to execute logic for e.g. evaluating whether preconditions for certain tasks are met or whether guardrails have been breached
2. **Separation of task and execution semantics**
   * Nodes describe what happens; bindings describe how and where it runs. For example, a "rest call" node contains runtime-invariant properties such as "URL", while the binding specifies which platform service will handle dispatch and processing of the service call itself.
   * The same process can therefore be re-bound to different platforms without model changes, on a per-node[-type] basis if required.  This allows incremental migration between platforms over time
   * Platforms may provide only a subset of bindings (e.g. a "Camunda 8" platform handling only deterministic BPMN 2.0 execution won't provide an "llm-prompt" binding), and presence of all required bindings on the target platform(s) can be verified at deployment time
   * Bindings follow a service discovery model.  E.g. a "rest_call" node may target the "service call" service on the "camunda_8" platform.  The orchestrator is aware of all available target platforms, and can delegate such requests to the appropriate service via service discovery (e.g. GS Discovery, API Catalog, or by allowing the target platform to perform routing)
   * Execution bindings include only the information required to route requests correctly to the execution platform. Implementation details are maintained within the target platform
3. **Agent action selection and dynamic workflows**
   * The specification supports dynamic generation of workflows by agents at runtime, within explicit guardrails and constraints.  Any node can be
     1. "Fixed", representing a single task bound to some execution semantics on the target platform.  This is the traditional case and the majority of nodes will be "fixed"
     2. A selection from one or more process fragments (i.e. other agent flow processes).  The model designer specifies the allowed fragments and their data contract
     3. A selection from one or more agent "tools", which it can also employ in any order
   * Given the similarities between (b) and (c), these are unified into a single "select" type where the unit of selection is an Agent Flow fragment.  A tools is a single-node fragment.  This allows reuse of all existing logic for data contracts, binding, constraints etc.
   * The model designer can specify "preconditions" for each selectable unit in the expression language of the orchestrator platform (by default, Python).  This determines whether the fragment/tool is presented to the agent as an available option.  This expression is evaluated based on the current model & data state each time the agent is presented with options
   * The "select" node specifies constraints on agent execution, for example the maximum number of invocations allowed, and can limit the specific data available to the agent
   * When invoking multiple fragments in this node, the AI agent will marshall data from step N to provide the input to step N+1. The runtime validates this against the declared node schema
4. **Generalized context and scope**
   * An execution boundary is defined to scope data visibility to a particular context.  The primary process defines a context, and each "select" node has its own context.
   * Child contexts inherit all data from their parent context by default.  This prevents local working data generated in a child process from polluting the parent context.  Data which should be passed to the parent is defined explicitly in the output schema
   * Although child contexts inherit all data from their parent context by default, they can also be explicitly limited to a subset of fields.  This allows sensitive data segregation and can also focus the scope of data which the sub-agent needs to deal with
5. **Explicit input/output data contracts**
   * The primary process, subprocess definitions (i.e. selectable fragments), and individual nodes all specify an input and output JSON schema.  This is used for programmatic validation and for agent marshalling of data in AI-driven selection nodes
   * Elements within a fully-specified data contract (e.g. individual nodes within a process that has defined input schema) can alternatively refer to data keys for simplicity and to avoid duplication/model fragility.  E.g. "$.order.id" where the parent process already contains an "order" object with "id" property
6. **Constraints and guardrails**
   * Process steps can be controlled with a number of constraints
     * Agent ability to dynamically select actions is limited by the set of sub-flows/tools provided to it, the data scope it is allowed to access, and other restrictions such as the number of iterations it is allowed to reach a conclusion
     * All nodes also have a "constraints" property to limit factors such as execution time, retry limits, cost or token limits, and custom guardrails.  These are platform-agnostic, e.g. a timeout constraint will apply to both LLM response generation and service call response time.  Some constraints are not relevant for certain node types
     * Custom guardrails can be defined using the expression language of the orchestrator platform, for example ensuring that `$.order.refundAmount <= $.order.purchaseAmount` at every step in agent execution, otherwise a "guardrail breached" exception will be thrown
7. **Agent flow definition vs resolved instance**
   * An Agent Flow model definition holds the statically-defined process, including all potential sources of dynamic content (e.g. agent "select" nodes)
   * The process will be resolved into a dynamically-generated instance at runtime as dynamic nodes are resolved into their selected elements
   * An audit/traceability schema will also be defined to reflect this dynamically-generated content.  This can be dynamically-generated for a specific Agent Flow schema based on the set of possible dynamic actions
8. **Audit and traceability**
   * The orchestrator platform will generate audit records of all actions, step transitions and events
   * It will also expose a schema for audit events which target platforms can publish.  These events are aggregated and associated with the relevant node by the orchestrator
9. **Exception model**
   * Agent Flow defines specific exception types (e.g. "service error", "guardrails breached") which nodes may generate.  These exceptions may be
     * Generated by the target platform, for example the target platform makes a service call and receives a 400 response, and its execution binding (internal to the platform) maps this failure type to a "service error"
     * Generated by the orchestrator, for example an agent-driven process breaches its guardrail by generating a refund amount exceeding the original purchase amount (as per the earlier example), or generates a response which fails a MNPI scan, in which case the orchestrator throws a "guardrails breached" exception
     * All nodes can specify "error_handlers" configuration which maps an exception type to a specific course of action, such a routing to a different node, or retrying execution.  Execution of the Agent Flow will terminate if no handler is defined for a thrown exception type
10. **Agent user interoperability**
    * Existing "user tasks" on a deterministic platform are targeted at individual or groups of users.  The user is expected to follow the task instructions, optionally provide data through an input form and/or respond to the task with one of several available actions
    * In this proposal, UI/form assets are modeled in the agent-interpretable A2UI format.  They render to form UIs which users can interact with, however agents are also able to intepret the form content and interact with it.
    * This enables the transition of certain user-driven tasks to agent ownership over time.  Agents should have a user identity in the runtime environment equivalent to human users, for traceability and accountability reasons, and so the routing strategy of user tasks can be updated to include agent users where appropriate.  The A2UI task content allows agents to interact with the task and action it, without any additional changes required to the model task definition
11. **Compatibility with existing deterministic workflow platforms**
    * An Agent Flow process represents a superset of traditional, deterministic flows such as BPMN 2.0.  Specifically, an Agent Flow process where every node has "fixed" type and an execution binding pointing to a deterministic BPMN 2.0 runtime platform would exactly represent this existing runtime
    * This enables incremental transition to an AI-enabled platform over time.  The execution binding for individual node types (in specific processes if desired, for A/B testing) can be updated to target the new AI-enabled platform.  Additional task types (such as "LLM prompt") can be made available by exposing the relevant bindings

---

## 2. Top-Level Structure

```
{
  "agent_flow": "1.0.0",
  "metadata": { ... },
  "client_id": "...",
  "input_schema": { ... },
  "output_schema": { ... },
  "nodes": [ ... ],
  "edges": [ ... ],
  "definitions": { ... },
  "assets": { ... },
  "bindings": { ... }
}
```

| Field | Required | Purpose |
|-------|----------|---------|
| `agent_flow` | yes | Specification version for forward compatibility |
| `metadata` | yes | Identity and description |
| `client_id` | yes | Identity of this agent to all platform services |
| `input_schema` | yes | JSON Schema for what the process receives |
| `output_schema` | yes | JSON Schema for what the process produces |
| `nodes` | yes | Activities in the graph |
| `edges` | yes | Transitions including conditional routing |
| `definitions` | no | Named reusable components (sub-flows and single-step operations) |
| `assets` | no | Locally bundled assets (UI forms, templates, etc.) |
| `bindings` | no | Platform component mappings (separable to own file) |

---

## 3. Metadata

```json
{
  "metadata": {
    "id": "order-fulfillment-v2",
    "name": "Order Fulfillment",
    "version": "2.2.0",
    "description": "Hybrid order processing with AI-assisted exception handling",
    "tags": ["fulfillment", "group-xyz"]
  }
}
```

| Field | Required | Purpose |
|-------|----------|---------|
| `id` | yes | Unique process identifier |
| `name` | yes | Human-readable name |
| `version` | yes | Process definition version (semver) |
| `description` | no | Human-readable description |
| `tags` | no | Freeform classification tags |

---

## 4. Client Identity

```json
{
  "client_id": "order-fulfillment-agent"
}
```

Identifies this agent to all platform services. Passed in every call the runtime makes. Used for authentication, authorization, rate limiting, audit trails, and cost attribution.

---

## 5. Input and Output Schemas

The process declares what it receives and what it produces. Intermediate state does not appear in either schema.

```json
{
  "input_schema": {
    "type": "object",
    "properties": {
      "order": { "type": "object", "required": ["id", "customer_id", "items", "total"] },
      "exception_notes": { "type": "string" }
    },
    "required": ["order"]
  },
  "output_schema": {
    "type": "object",
    "properties": {
      "validation_result": { "type": "string" },
      "resolution": { "type": "object" },
      "shipping_label": { "type": "object" }
    }
  }
}
```

---

## 6. Context Model

Every execution boundary creates a context scope with a uniform pattern.

### 6.1 Scope Diagram

```
┌─ Process Context ────────────────────────────────────────┐
│  inherited: (full input state)                           │
│  locals: (created by nodes at runtime, not declared)     │
│  outputs: (per output_schema)                            │
│                                                          │
│  ┌─ select iteration context ─────────────────────────┐  │
│  │  inherited: full parent (default) or limit_to [..] │  │
│  │  locals: (created at runtime)                      │  │
│  │  outputs: [resolution]                             │  │
│  │                                                    │  │
│  │  ┌─ Sub-flow context ───────────────────────────┐  │  │
│  │  │  inherited: full parent (default)             │  │  │
│  │  │  locals: (created at runtime)                 │  │  │
│  │  │  outputs: declared per definition             │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### 6.2 Context Declaration

Full parent inheritance is the default. Declare a `context` block only to restrict inheritance or declare outputs.

```json
"context": {
  "limit_to": ["order", "exception_notes"],
  "outputs": ["resolution"]
}
```

| Property | Default | Purpose |
|----------|---------|---------|
| `limit_to` | All parent keys | Restricts which parent keys are visible |
| `outputs` | None | Keys that merge back to parent when scope closes |

Locals are created by steps at runtime and discarded when the scope closes.

### 6.3 Resolution Rules

1. On scope open: parent keys are available (all by default, or restricted by `limit_to`).
2. Nodes read inherited keys and any locals created during execution.
3. Nodes write to locals freely and to declared output keys.
4. On scope close: `outputs` merge to parent. Everything else is discarded.

---

## 7. Nodes

### 7.1 Common Properties

```json
{
  "id": "validate-order",
  "name": "Validate Order",
  "activity_type": "task",
  "task_type": "rest_call",
  "config": { ... },
  "mode": "fixed",
  "inputs": ["order"],
  "outputs": ["validation_result"],
  "constraints": { ... },
  "error_handlers": [ ... ],
  "metadata": { ... }
}
```

| Field | Required | Purpose |
|-------|----------|---------|
| `id` | yes | Unique within containing flow |
| `name` | yes | Human-readable label |
| `activity_type` | yes | Structural kind of work |
| `task_type` | no | Kind of task (for `task` activity types) |
| `config` | no | Runtime-invariant parameters |
| `mode` | yes | Execution mode |
| `inputs` | no | State keys read (key list) or input schema (JSON Schema) |
| `outputs` | no | State keys written (key list) or output schema (JSON Schema) |
| `context` | no | Scope configuration for select/subprocess modes |
| `constraints` | no | Timeouts, retries, cost/token limits, guardrails |
| `error_handlers` | no | Error routing |
| `metadata` | no | Freeform annotations |

Any node can use `$ref` for external definitions:

```json
{ "id": "validate-order", "$ref": "./nodes/validate-order.json" }
```

### 7.2 Activity Types

| Activity Type | Semantics | Traditional Equivalent |
|---------------|-----------|----------------------|
| `"task"` | Unit of work producing output | Service Task / Script Task |
| `"user_interaction"` | Requires human (or agent) input | User Task |
| `"llm_prompt"` | Invokes an LLM with a prompt template | *(none)* |
| `"decision"` | Selects an outgoing path (routing anchor) | Exclusive Gateway |
| `"parallel_split"` | Fans out to concurrent paths | Parallel Gateway (fork) |
| `"parallel_join"` | Waits for concurrent paths | Parallel Gateway (join) |
| `"subprocess"` | Invokes a sub-flow | Call Activity |
| `"event_wait"` | Pauses for external event | Intermediate Catch Event |
| `"emit_event"` | Publishes an event | Intermediate Throw Event |
| `"start"` | Entry point | Start Event |
| `"end"` | Terminal point | End Event |

### 7.3 Task Types and Config

For `task` activity type nodes, `task_type` describes the kind of work. `config` carries runtime-invariant parameters.

| Task Type | Purpose |
|-----------|---------|
| `rest_call` | HTTP service invocation |
| `grpc_call` | gRPC service invocation |
| `notification` | User-facing notification |
| `event_publish` | Publish to event bus |
| `script` | Run a deterministic script |
| `invoke_agent` | Call another agent definition |

#### `rest_call`

```json
{
  "task_type": "rest_call",
  "config": {
    "url": "https://inventory.internal/api/validate",
    "method": "POST",
    "input_mapping": { "body.items": "$.order.items" },
    "output_mapping": { "validation_result": "$.response.result" }
  }
}
```

#### `notification`

```json
{
  "task_type": "notification",
  "config": {
    "channel": "customer-email",
    "template": "order-rejected",
    "recipient_mapping": "$.order.customer_email",
    "params_mapping": { "order_id": "$.order.id" }
  }
}
```

#### `event_publish`

```json
{
  "task_type": "event_publish",
  "config": {
    "topic": "manager-notifications",
    "event_type": "exception-review-requested",
    "payload_mapping": { "order_id": "$.order.id" }
  }
}
```

#### `script`

The node declares its language and source. The binding component declares which languages the platform supports. Mismatches are caught at deployment validation.

```json
{
  "task_type": "script",
  "config": {
    "language": "python",
    "source": "state['order']['total_normalized'] = round(state['order']['total'], 2)"
  }
}
```

Script `language` is independent from the top-level `expression_language`. Expressions are evaluated by the orchestrator; scripts are executed by the platform's script runner.

#### `invoke_agent`

```json
{
  "task_type": "invoke_agent",
  "config": {
    "agent_ref": "./agents/fraud-check.json",
    "input_mapping": { "order": "$.order" },
    "output_mapping": { "fraud_score": "$.result.score" }
  }
}
```

### 7.4 `llm_prompt` Activity Type

The model is selected per task by the process author; the platform routes to the appropriate provider.

```json
{
  "activity_type": "llm_prompt",
  "config": {
    "model": "claude-sonnet-4-20250514",
    "template": "Classify this exception as one of: [damaged_goods, wrong_item, missing_item, billing_error, other].\n\nOrder: {{order}}\nException: {{exception_notes}}",
    "input_mapping": {
      "order": "$.order",
      "exception_notes": "$.exception_notes"
    },
    "output_key": "exception_classification",
    "output_schema": {
      "type": "string",
      "enum": ["damaged_goods", "wrong_item", "missing_item", "billing_error", "other"]
    }
  },
  "mode": "fixed"
}
```

### 7.5 `event_wait` Activity Type

```json
{
  "activity_type": "event_wait",
  "config": {
    "event_type": "customer-reply",
    "correlation_key": "$.order.id",
    "timeout": { "duration": 1, "unit": "days" }
  },
  "mode": "fixed"
}
```

### 7.6 `user_interaction` Activity Type

Models tasks requiring input from a human user or an agent.

```json
{
  "activity_type": "user_interaction",
  "config": {
    "ui": {
      "type": "local",
      "id": "order-exception-review"
    },
    "routing_strategy": {
      "type": "distribution_list",
      "id": "order-exception-reviewers@internal"
    },
    "priority": "HIGH",
    "task_name": "Review exception for order {{order.id}}",
    "task_description": "Order total: {{order.total}} {{order.currency}}. Exception: {{exception_notes}}",
    "input_mapping": {
      "order_summary": "$.review_summary",
      "order_items": "$.order.items",
      "exception_notes": "$.exception_notes"
    },
    "available_actions": [
      {
        "id": "approve_refund",
        "label": "Approve Refund",
        "availability_condition": {
          "expression": "state.get('order', {}).get('total', 0) < 1000"
        }
      },
      {
        "id": "reject",
        "label": "Reject Exception"
      },
      {
        "id": "escalate",
        "label": "Escalate to Manager"
      },
      {
        "id": "cancel",
        "label": "Cancel"
      }
    ],
    "output_mapping": {
      "review_action": "$.action_id",
      "review_data": "$.form_data",
      "reviewer_id": "$.completed_by"
    }
  },
  "mode": "fixed"
}
```

#### UI Reference

The `ui` property references the form definition presented to the task recipient.

| `type` | `id` meaning | Resolution |
|--------|-------------|------------|
| `local` | Key in the model's `assets` section | Resolved at build/deploy time |
| `ref` | URI or relative path to an external asset | Fetched from external location |
| `deployed` | ID of an asset already on the UI server | Resolved at runtime |

#### Routing Strategy

| `type` | Purpose |
|--------|---------|
| `user_list` | Specific named users |
| `distribution_list` | A managed group of recipients |
| `organizational_unit` | An org hierarchy unit |

Extensible to `agent` / `agent_pool` routing types. When UI definitions use an agent-interpretable format (e.g., A2UI), no additional agent-specific configuration is needed — the task structure works identically for human and agent recipients.

#### Available Actions

Each action defines something the recipient can do. `availability_condition` is an optional expression — if false, the action is not presented. Actions without it are always available.

---

## 8. Execution Modes

### 8.1 `fixed` — No Autonomy

```json
"mode": "fixed"
```

### 8.2 `select` — AI Selects From Options

```json
"mode": {
  "type": "select",
  "instruction": "Resolve this customer exception...",
  "options": [ ... ],
  "selection_mode": "iterative",
  "exit_condition": "resolution.action_taken is set",
  "max_iterations": 5
}
```

| Property | Required | Purpose |
|----------|----------|---------|
| `type` | yes | Always `"select"` |
| `instruction` | yes | System prompt for the selecting LLM |
| `options` | yes | Available choices (references to `definitions`) |
| `selection_mode` | yes | `"single"`, `"iterative"`, or `"parallel"` |
| `exit_condition` | no | When to stop (for iterative) |
| `max_iterations` | no | Hard cap on iterations |

#### Selection Modes

| Mode | Behavior |
|------|----------|
| `"single"` | Pick one option, execute, done. |
| `"iterative"` | Loop: pick → execute → inspect → pick another or exit. |
| `"parallel"` | Pick multiple, execute concurrently, merge results. |

#### Option Properties

| Property | Required | Purpose |
|----------|----------|---------|
| `ref` | yes | ID of a definition |
| `description` | yes | Shown to LLM during selection |
| `precondition` | no | Deterministic filter expression |

#### LLM Input Marshalling

At each step the LLM produces:

```json
{
  "option": "auto-refund",
  "input": { "order": { "...marshalled from state..." } },
  "reasoning": "Low-value order, clear defect."
}
```

The runtime validates `input` against the definition's `inputs` schema.

### 8.3 `none` — Decision Nodes

Decision nodes are structural routing anchors. Routing logic lives on outbound edges.

```json
"mode": "none"
```

### 8.4 Mode Shape

`mode` is polymorphic: a string for `"fixed"` and `"none"`, an object for `"select"`.

---

## 9. Edges

### 9.1 Basic Edges

```json
{ "from": "start", "to": "validate-order" }
```

### 9.2 Conditional Edges

```json
{
  "from": "route-validation",
  "to": "generate-label",
  "condition": { "expression": "state['validation_result'] == 'pass'" }
}
```

Default edge:

```json
{ "from": "route-validation", "to": "handle-exception", "condition": { "default": true } }
```

### 9.3 LLM-Driven Routing

Recommended pattern: an `llm_prompt` node writes a routing value to state, then a deterministic decision node routes on it.

### 9.4 Edge Properties

| Field | Required | Purpose |
|-------|----------|---------|
| `from` | yes | Source node ID |
| `to` | yes | Target node ID |
| `condition` | no | Routing condition |
| `metadata` | no | Annotations |

---

## 10. Definitions

Named, reusable components referenced by "select" options, nodes, or "$ref". Every definition has the same structure: an interface contract (inputs/outputs) and a flow (nodes/edges).

Conceptually, an agent may have access to individual "tools", or may be allowed to select pre-defined "process fragments" for execution. These are unified into a single "process fragment" concept: a "tool" is simply a single-node process fragment. This allows a unified approach to all agent-invokable activities.
### 10.1 Structure

```json
{
  "definitions": {
    "definition-id": {
      "description": "...",
      "inputs": { "type": "object", "properties": { ... } },
      "outputs": { "type": "object", "properties": { ... } },
      "context": { ... },
      "nodes": [ ... ],
      "edges": [ ... ]
    }
  }
}
```

Internal edges use `__start__` and `__end__` as entry/exit pseudo-nodes.

### 10.2 External References

```json
{
  "definitions": {
    "escalate-to-manager": { "$ref": "./definitions/escalate-to-manager.json" }
  }
}
```

---

## 11. Assets

Locally bundled assets referenced by nodes. Each asset has a `class` (the category of asset), a `type` (the specific format), and `content`.

```json
{
  "assets": {
    "order-exception-review": {
      "class": "ui",
      "type": "a2ui",
      "content": { ... }
    }
  }
}
```

| Property | Required | Purpose |
|----------|----------|---------|
| `class` | yes | Asset category (e.g., `"ui"` for form definitions) |
| `type` | yes | Specific format within the class (e.g., `"a2ui"`) |
| `content` | yes | The asset payload. Shape depends on class and type. |

The `class` + `type` separation allows multiple formats within the same category. For example, a `"ui"` class asset might be `"a2ui"` format today and a different format in future.

Assets are referenced from node configs:

```json
"ui": { "type": "local", "id": "order-exception-review" }
```

The `assets` section is extensible — new classes can be added as needed (e.g., `"template"` for notification templates, `"schema"` for reusable data schemas).

---

## 12. Constraints

A general-purpose node-level property for execution boundaries.

```json
"constraints": {
  "timeout": { "duration": 5, "unit": "minutes" },
  "max_retries": 3,
  "retry_backoff_seconds": [1, 5, 15],
  "cost_limit_usd": 1.00,
  "token_limit": 10000,
  "guardrails": [
    { "type": "content_filter", "config": { "block_pii": true } },
    {
      "type": "custom",
      "description": "Refunds must not exceed order total",
      "expression": "state.get('resolution', {}).get('refund_amount', 0) <= state['order']['total']"
    }
  ]
}
```

### Timeout

```json
"timeout": { "duration": 30, "unit": "seconds" }
```

| `unit` | Typical use |
|--------|-------------|
| `"seconds"` | Short-lived operations |
| `"minutes"` | Standard tasks |
| `"hours"` | Long-running tasks |
| `"days"` | Human tasks, async waits |

### Constraint Properties

| Property | Applies To | Purpose |
|----------|-----------|---------|
| `timeout` | Any node | Maximum execution time |
| `max_retries` | Any node | Retry attempts |
| `retry_backoff_seconds` | Any node | Backoff schedule |
| `cost_limit_usd` | AI-driven nodes | Maximum LLM spend |
| `token_limit` | AI-driven nodes | Maximum token consumption |
| `guardrails` | AI-driven nodes | Content filters and custom rules |

---

## 13. Error Handlers

Error types are defined by the platform. The process contains only per-node handlers.

### 13.1 Standard Error Types (Platform-Defined)

| Error Type | Trigger | Retryable |
|------------|---------|-----------|
| `service_error` | External service call failed | yes |
| `validation_error` | Output failed schema validation | no |
| `timeout_error` | Node exceeded timeout | yes |
| `guardrail_violation` | Constraint/guardrail check failed | conditional |
| `cost_limit_exceeded` | AI node exceeded cost limit | no |
| `token_limit_exceeded` | AI node exceeded token limit | no |
| `no_selection_reached` | Select mode hit max_iterations without exit condition | no |
| `internal_error` | Unexpected runtime exception | no |

### 13.2 Handler Actions

| Action | Behavior |
|--------|----------|
| `"retry"` | Re-execute using node's constraint retry config |
| `"route"` | Redirect to specified node |
| `"abort"` | Terminate agent execution |
| `"emit_event"` | Publish error event |

---

## 14. Bindings

Bindings map task types to platform components.

### 14.1 Structure

```json
{
  "bindings": {
    "runtime": "adk",
    "runtime_version": "1.0.0",
    "expression_language": "python",
    "components": { ... }
  }
}
```

### 14.2 What Goes Where

| Detail | Location | Rationale |
|--------|----------|-----------|
| Service URL, method, path | Node `config` | Property of the service |
| Event topic, event type | Node `config` | Property of the event architecture |
| Prompt template, model | Node `config` | Task-level design choice |
| Which platform handles a task type | Binding `components` | Deployment decision |
| Default sender, supported languages | Binding `config` | Platform-level defaults |
| Auth, transport, proxies | Platform-internal | Not exposed |

### 14.3 Expression Language Scope

`expression_language` governs inline expressions in the process model — edge conditions, preconditions, guardrail expressions, availability conditions. Evaluated by the orchestrator.

### 14.4 Script Language Support

```json
"script": {
  "platform": "ai_platform",
  "target_version": "2.1.0",
  "service": "script_runner",
  "config": { "supported_languages": ["python"] }
}
```

---

## 15. Runtime Mapping Reference

### 15.1 Orchestrator (ADK / LangGraph)

Below is an example mapping from specification concepts to the equivalent in LangGraph.  LangGraph is shown here given the more direct correlation from these processes to the graph-based abstraction in LangGraph, but an equivalent mapping can be derived for ADK.

| Spec Concept | LangGraph Equivalent |
|---|---|
| Process | `StateGraph` |
| `input_schema` / `output_schema` | `TypedDict` / Pydantic model |
| Context scope | State channel filtering on subgraph entry/exit |
| `fixed` node | Regular node function |
| `llm_prompt` node | Model invocation via LLM gateway |
| `decision` + conditional edges | `add_conditional_edges()` |
| `select` node | Selector loop: LLM → validate → execute → check exit → repeat |
| Definition (multi-node) | Compiled sub-`StateGraph` |
| Definition (single-node) | Direct function call |

### 15.2 Traditional Workflow Engine (e.g., Camunda)

| Spec Concept | Camunda Equivalent |
|---|---|
| `task` + `fixed` | Service Task / External Task |
| `user_interaction` + `fixed` | User Task |
| `decision` + conditional edges | Exclusive Gateway + Sequence Flows |
| `parallel_split` / `parallel_join` | Parallel Gateway |
| `event_wait` | Intermediate Message Catch Event |
| Definition (multi-node) | Embedded Sub-Process |
| `select` mode | **No equivalent** (flagged on validation) |
| `llm_prompt` | **No equivalent** (flagged on validation) |

---

## 16. Full Example: Hybrid Order Fulfillment

```json
{
  "agent_flow": "1.0.0",

  "metadata": {
    "id": "order-fulfillment-v2",
    "name": "Order Fulfillment",
    "version": "2.2.0",
    "description": "Hybrid order processing with AI-assisted exception handling",
    "tags": ["fulfillment", "group-xyz"]
  },

  "client_id": "order-fulfillment-agent",

  "input_schema": {
    "type": "object",
    "properties": {
      "order": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "customer_id": { "type": "string" },
          "customer_email": { "type": "string" },
          "items": { "type": "array" },
          "total": { "type": "number" },
          "currency": { "type": "string" }
        },
        "required": ["id", "customer_id", "items", "total"]
      },
      "exception_notes": { "type": "string" }
    },
    "required": ["order"]
  },

  "output_schema": {
    "type": "object",
    "properties": {
      "validation_result": { "type": "string", "enum": ["pass", "fail", "needs_review"] },
      "resolution": { "type": "object" },
      "shipping_label": { "type": "object" }
    }
  },

  "nodes": [
    {
      "id": "start",
      "name": "Order Received",
      "activity_type": "start"
    },
    {
      "id": "normalize-order",
      "name": "Normalize Order Data",
      "activity_type": "task",
      "task_type": "script",
      "config": {
        "language": "python",
        "source": "state['order']['total_normalized'] = round(state['order']['total'], 2)\nstate['order']['item_count'] = len(state['order']['items'])\nif not state['order'].get('currency'):\n    state['order']['currency'] = 'USD'"
      },
      "mode": "fixed",
      "inputs": ["order"],
      "outputs": ["order"]
    },
    {
      "id": "validate-order",
      "name": "Validate Order",
      "activity_type": "task",
      "task_type": "rest_call",
      "config": {
        "url": "https://inventory.internal/api/validate",
        "method": "POST",
        "input_mapping": { "body.items": "$.order.items" },
        "output_mapping": { "validation_result": "$.response.result" }
      },
      "mode": "fixed",
      "inputs": ["order"],
      "outputs": ["validation_result"],
      "constraints": {
        "timeout": { "duration": 30, "unit": "seconds" },
        "max_retries": 3,
        "retry_backoff_seconds": [1, 5, 15]
      },
      "error_handlers": [
        { "error_type": "service_error", "action": "retry" }
      ]
    },
    {
      "id": "route-validation",
      "name": "Route by Validation Result",
      "activity_type": "decision",
      "mode": "none",
      "inputs": ["validation_result"]
    },
    {
      "id": "generate-label",
      "name": "Generate Shipping Label",
      "activity_type": "task",
      "task_type": "rest_call",
      "config": {
        "url": "https://shipping.internal/api/labels",
        "method": "POST",
        "input_mapping": {
          "body.order_id": "$.order.id",
          "body.items": "$.order.items"
        },
        "output_mapping": { "shipping_label": "$.response" }
      },
      "mode": "fixed",
      "inputs": ["order"],
      "outputs": ["shipping_label"],
      "constraints": {
        "timeout": { "duration": 1, "unit": "minutes" }
      },
      "error_handlers": [
        { "error_type": "service_error", "action": "retry" },
        { "error_type": "timeout_error", "action": "route", "target": "handle-exception" }
      ]
    },
    {
      "id": "reject-order",
      "name": "Reject Order",
      "activity_type": "task",
      "task_type": "notification",
      "config": {
        "channel": "customer-email",
        "template": "order-rejected",
        "recipient_mapping": "$.order.customer_email",
        "params_mapping": {
          "order_id": "$.order.id",
          "reason": "$.validation_result"
        }
      },
      "mode": "fixed",
      "inputs": ["order", "validation_result"]
    },
    {
      "id": "handle-exception",
      "name": "Handle Exception (AI-Assisted)",
      "activity_type": "task",
      "mode": {
        "type": "select",
        "instruction": "You are resolving a customer order exception. Analyze the order and exception details. Use lookup tools to gather information, then choose a resolution approach. After each action, evaluate whether the issue is fully resolved.",
        "options": [
          {
            "ref": "lookup-order-history",
            "description": "Retrieves past orders for the customer. Useful for identifying patterns."
          },
          {
            "ref": "check-inventory",
            "description": "Checks stock levels for relevant SKUs. Helps determine if replacement is possible."
          },
          {
            "ref": "escalate-to-manager",
            "description": "Routes to a human manager. Best for high-value or sensitive issues."
          },
          {
            "ref": "auto-refund",
            "description": "Automatically issues a refund. For straightforward, low-value cases.",
            "precondition": {
              "expression": "state.get('order', {}).get('total', 0) < 100"
            }
          },
          {
            "ref": "request-more-info",
            "description": "Asks the customer for clarification. Use when the situation is ambiguous."
          },
          {
            "ref": "manual-review",
            "description": "Sends to a human reviewer for inspection and decision. Use for complex or unusual cases."
          }
        ],
        "selection_mode": "iterative",
        "exit_condition": "resolution.action_taken is set",
        "max_iterations": 5
      },
      "inputs": ["order", "validation_result", "exception_notes"],
      "outputs": ["resolution"],
      "context": {
        "outputs": ["resolution"]
      },
      "constraints": {
        "timeout": { "duration": 10, "unit": "minutes" },
        "cost_limit_usd": 1.00,
        "guardrails": [
          {
            "type": "custom",
            "description": "Total refund must not exceed order total",
            "expression": "state.get('resolution', {}).get('refund_amount', 0) <= state['order']['total']"
          }
        ]
      },
      "error_handlers": [
        { "error_type": "cost_limit_exceeded", "action": "route", "target": "reject-order" },
        { "error_type": "guardrail_violation", "action": "retry", "max_attempts": 2 },
        { "error_type": "no_selection_reached", "action": "route", "target": "reject-order" }
      ]
    },
    {
      "id": "end",
      "name": "Order Complete",
      "activity_type": "end"
    }
  ],

  "edges": [
    { "from": "start", "to": "normalize-order" },
    { "from": "normalize-order", "to": "validate-order" },
    { "from": "validate-order", "to": "route-validation" },
    {
      "from": "route-validation", "to": "generate-label",
      "condition": { "expression": "state['validation_result'] == 'pass'" }
    },
    {
      "from": "route-validation", "to": "reject-order",
      "condition": { "expression": "state['validation_result'] == 'fail'" }
    },
    {
      "from": "route-validation", "to": "handle-exception",
      "condition": { "default": true }
    },
    { "from": "handle-exception", "to": "end" },
    { "from": "generate-label", "to": "end" },
    { "from": "reject-order", "to": "end" }
  ],

  "definitions": {

    "escalate-to-manager": {
      "description": "Routes exception to a human manager for review",
      "inputs": {
        "type": "object",
        "properties": { "order": { "type": "object" }, "exception_notes": { "type": "string" } },
        "required": ["order"]
      },
      "outputs": {
        "type": "object",
        "properties": { "resolution": { "type": "object" } }
      },
      "nodes": [
        {
          "id": "notify-manager", "name": "Notify Manager", "activity_type": "task", "task_type": "event_publish",
          "config": {
            "topic": "manager-notifications", "event_type": "exception-review-requested",
            "payload_mapping": { "order_id": "$.order.id", "exception": "$.exception_notes" }
          },
          "mode": "fixed", "inputs": ["order", "exception_notes"]
        },
        {
          "id": "await-decision", "name": "Await Manager Decision", "activity_type": "event_wait",
          "config": { "event_type": "manager-decision", "correlation_key": "$.order.id" },
          "mode": "fixed", "outputs": ["resolution"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "notify-manager" },
        { "from": "notify-manager", "to": "await-decision" },
        { "from": "await-decision", "to": "__end__" }
      ]
    },

    "auto-refund": {
      "description": "Issues a refund and sends confirmation",
      "inputs": {
        "type": "object",
        "properties": { "order": { "type": "object" } },
        "required": ["order"]
      },
      "outputs": {
        "type": "object",
        "properties": { "resolution": { "type": "object" } }
      },
      "nodes": [
        {
          "id": "issue-refund", "name": "Issue Refund", "activity_type": "task", "task_type": "rest_call",
          "config": {
            "url": "https://payments.internal/api/refund", "method": "POST",
            "input_mapping": { "body.amount": "$.order.total", "body.order_id": "$.order.id" },
            "output_mapping": { "resolution.action_taken": "'auto_refund'", "resolution.refund_amount": "$.response.amount" }
          },
          "mode": "fixed", "inputs": ["order"], "outputs": ["resolution"]
        },
        {
          "id": "send-confirmation", "name": "Send Confirmation", "activity_type": "task", "task_type": "notification",
          "config": {
            "channel": "customer-email", "template": "refund-issued",
            "recipient_mapping": "$.order.customer_email",
            "params_mapping": { "order_id": "$.order.id", "refund_amount": "$.resolution.refund_amount" }
          },
          "mode": "fixed", "inputs": ["order", "resolution"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "issue-refund" },
        { "from": "issue-refund", "to": "send-confirmation" },
        { "from": "send-confirmation", "to": "__end__" }
      ]
    },

    "request-more-info": {
      "description": "AI drafts a clarification question and waits for customer reply",
      "inputs": {
        "type": "object",
        "properties": { "order": { "type": "object" }, "exception_notes": { "type": "string" } },
        "required": ["order", "exception_notes"]
      },
      "outputs": {
        "type": "object",
        "properties": { "customer_reply": { "type": "string" } }
      },
      "nodes": [
        {
          "id": "draft-question", "name": "Draft Clarification", "activity_type": "llm_prompt",
          "config": {
            "model": "claude-sonnet-4-20250514",
            "template": "Based on the following order and exception, draft a clear, polite question to the customer asking for the specific information needed.\n\nOrder: {{order}}\nException: {{exception_notes}}\n\nDraft a brief email body only.",
            "input_mapping": { "order": "$.order", "exception_notes": "$.exception_notes" },
            "output_key": "draft_message"
          },
          "mode": "fixed", "inputs": ["order", "exception_notes"], "outputs": ["draft_message"]
        },
        {
          "id": "send-question", "name": "Send to Customer", "activity_type": "task", "task_type": "notification",
          "config": {
            "channel": "customer-email", "template": "clarification-question",
            "recipient_mapping": "$.order.customer_email",
            "params_mapping": { "body": "$.draft_message" }
          },
          "mode": "fixed", "inputs": ["order", "draft_message"]
        },
        {
          "id": "await-reply", "name": "Await Customer Reply", "activity_type": "event_wait",
          "config": { "event_type": "customer-reply", "correlation_key": "$.order.id", "timeout": { "duration": 1, "unit": "days" } },
          "mode": "fixed", "outputs": ["customer_reply"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "draft-question" },
        { "from": "draft-question", "to": "send-question" },
        { "from": "send-question", "to": "await-reply" },
        { "from": "await-reply", "to": "__end__" }
      ]
    },

    "manual-review": {
      "description": "Sends order to a human reviewer for inspection and decision. Use for complex or unusual cases.",
      "inputs": {
        "type": "object",
        "properties": { "order": { "type": "object" }, "exception_notes": { "type": "string" } },
        "required": ["order"]
      },
      "outputs": {
        "type": "object",
        "properties": { "resolution": { "type": "object" } }
      },
      "nodes": [
        {
          "id": "prepare-review-data", "name": "Prepare Review Summary", "activity_type": "task", "task_type": "script",
          "config": {
            "language": "python",
            "source": "state['review_summary'] = {\n    'order_id': state['order']['id'],\n    'customer_id': state['order']['customer_id'],\n    'total': state['order']['total'],\n    'item_count': len(state['order']['items']),\n    'exception': state.get('exception_notes', 'No notes'),\n    'high_value': state['order']['total'] > 500\n}"
          },
          "mode": "fixed", "inputs": ["order", "exception_notes"], "outputs": ["review_summary"]
        },
        {
          "id": "review-task", "name": "Human Review", "activity_type": "user_interaction",
          "config": {
            "ui": { "type": "local", "id": "order-exception-review" },
            "routing_strategy": { "type": "distribution_list", "id": "order-exception-reviewers@internal" },
            "priority": "HIGH",
            "task_name": "Review exception for order {{order.id}}",
            "task_description": "Order total: {{order.total}} {{order.currency}}. Exception: {{exception_notes}}",
            "input_mapping": { "order_summary": "$.review_summary", "order_items": "$.order.items", "exception_notes": "$.exception_notes" },
            "available_actions": [
              { "id": "approve_refund", "label": "Approve Refund", "availability_condition": { "expression": "state.get('order', {}).get('total', 0) < 1000" } },
              { "id": "reject", "label": "Reject Exception" },
              { "id": "escalate", "label": "Escalate to Manager" },
              { "id": "cancel", "label": "Cancel" }
            ],
            "output_mapping": { "review_action": "$.action_id", "review_data": "$.form_data", "reviewer_id": "$.completed_by" }
          },
          "mode": "fixed", "inputs": ["review_summary", "order", "exception_notes"],
          "outputs": ["review_action", "review_data", "reviewer_id"],
          "constraints": { "timeout": { "duration": 2, "unit": "days" } }
        },
        {
          "id": "apply-review-decision", "name": "Apply Review Decision", "activity_type": "task", "task_type": "script",
          "config": {
            "language": "python",
            "source": "action = state['review_action']\ndata = state['review_data']\nif action == 'approve_refund':\n    state['resolution'] = {\n        'action_taken': 'manual_refund',\n        'refund_amount': data['refund_amount'],\n        'notes': data.get('refund_reason', ''),\n        'reviewed_by': state['reviewer_id']\n    }\nelif action == 'reject':\n    state['resolution'] = {\n        'action_taken': 'rejected',\n        'notes': data.get('rejection_reason', ''),\n        'reviewed_by': state['reviewer_id']\n    }\nelif action == 'escalate':\n    state['resolution'] = {\n        'action_taken': 'escalated',\n        'notes': data.get('escalation_notes', ''),\n        'reviewed_by': state['reviewer_id']\n    }"
          },
          "mode": "fixed", "inputs": ["review_action", "review_data", "reviewer_id"], "outputs": ["resolution"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "prepare-review-data" },
        { "from": "prepare-review-data", "to": "review-task" },
        { "from": "review-task", "to": "apply-review-decision" },
        { "from": "apply-review-decision", "to": "__end__" }
      ]
    },

    "lookup-order-history": {
      "description": "Retrieves full order history for a customer",
      "inputs": {
        "type": "object",
        "properties": { "customer_id": { "type": "string" } },
        "required": ["customer_id"]
      },
      "outputs": {
        "type": "object",
        "properties": { "order_history": { "type": "array", "items": { "type": "object" } } }
      },
      "nodes": [
        {
          "id": "lookup", "name": "Lookup Order History", "activity_type": "task", "task_type": "rest_call",
          "config": {
            "url": "https://crm.internal/api/orders", "method": "GET",
            "input_mapping": { "query.customer_id": "$.customer_id" },
            "output_mapping": { "order_history": "$.response.orders" }
          },
          "mode": "fixed", "inputs": ["customer_id"], "outputs": ["order_history"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "lookup" },
        { "from": "lookup", "to": "__end__" }
      ]
    },

    "check-inventory": {
      "description": "Checks current stock levels for given SKUs",
      "inputs": {
        "type": "object",
        "properties": { "skus": { "type": "array", "items": { "type": "string" } } },
        "required": ["skus"]
      },
      "outputs": {
        "type": "object",
        "properties": { "stock_levels": { "type": "object", "additionalProperties": { "type": "integer" } } }
      },
      "nodes": [
        {
          "id": "check", "name": "Check Inventory", "activity_type": "task", "task_type": "rest_call",
          "config": {
            "url": "https://inventory.internal/api/stock", "method": "POST",
            "input_mapping": { "body.skus": "$.skus" },
            "output_mapping": { "stock_levels": "$.response.levels" }
          },
          "mode": "fixed", "inputs": ["skus"], "outputs": ["stock_levels"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "check" },
        { "from": "check", "to": "__end__" }
      ]
    }
  },

  "assets": {
    "order-exception-review": {
      "class": "ui",
      "type": "a2ui",
      "content": { ... }
    }
  },

  "bindings": {
    "runtime": "adk",
    "runtime_version": "1.0.0",
    "expression_language": "python",

    "components": {
      "rest_call": {
        "platform": "camunda_8",
        "target_version": "1.0.0",
        "service": "service_gateway",
        "config": {}
      },
      "notification": {
        "platform": "camunda_8",
        "target_version": "1.0.0",
        "service": "notification_service",
        "config": { "default_from": "noreply@notifications.internal" }
      },
      "event_publish": {
        "platform": "ai_platform",
        "target_version": "2.1.0",
        "service": "event_bus",
        "config": {}
      },
      "event_wait": {
        "platform": "ai_platform",
        "target_version": "2.1.0",
        "service": "event_bus",
        "config": {}
      },
      "llm_prompt": {
        "platform": "ai_platform",
        "target_version": "2.1.0",
        "service": "llm_gateway",
        "config": {}
      },
      "user_interaction": {
        "platform": "camunda_8",
        "target_version": "1.0.0",
        "service": "tasks",
        "config": {}
      },
      "script": {
        "platform": "ai_platform",
        "target_version": "2.1.0",
        "service": "script_runner",
        "config": { "supported_languages": ["python"] }
      }
    }
  }
}
```
