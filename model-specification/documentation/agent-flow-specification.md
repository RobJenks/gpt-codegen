# Agent Flow Specification

**Spec Version:** 1.0.0-draft
**Status:** Proposal

---

## 1. Key Design Principles

### 1.1 Hybrid Execution Across Deterministic and AI Runtimes

A single process definition can target multiple execution platforms simultaneously. Deterministic tasks (service calls, notifications, human tasks) can execute on traditional workflow engines like Camunda, while AI-driven tasks (LLM prompts, autonomous selection) execute on an AI platform. A LangGraph-based orchestrator coordinates across all platforms, treating each as a component.

A fully deterministic BPMN-style process is a valid instance of this spec — it is simply an agent where every node uses `fixed` mode and all components target a traditional workflow engine. An autonomous AI agent is the other extreme. Everything in between is the same schema with different mode and binding settings.

### 1.2 Separation of Task Semantics and Execution

Each node describes **what** should happen (activity type, task type, configuration) independently of **how** it executes. Bindings map task types to platform components, and the same process definition can be re-bound to different platforms without changing the model. A node that says "POST to this URL" is a modeling statement; the binding says which platform's service gateway handles it.

Node configuration carries runtime-invariant details — service URLs, event topics, prompt templates, notification channels. These are properties of the services being called, not the platform doing the calling. Bindings carry only platform-specific concerns: which platform, which service within it, and any platform-level configuration.

### 1.3 Unified Selection Model for Dynamic Workflows

AI-driven nodes use a single `select` mode that presents the LLM with a menu of options. Options can be multi-step sub-flows (fragments) or single-step operations (tools) — both are defined identically as entries in the `definitions` section. The LLM sees a uniform menu; the runtime knows how to execute each type. This consolidates what would otherwise be separate "choose a sub-process" and "pick from a tool bag" mechanisms into one concept.

Selection supports three modes: `single` (pick one, done), `iterative` (pick, execute, evaluate, repeat until exit condition or max iterations), and `parallel` (pick multiple, execute concurrently). At each selection step, the LLM marshals the input payload for its chosen option from the current context state, and the runtime validates it against the option's declared input schema.

### 1.4 Generalized Context and Scope

Every execution boundary — the top-level process, a `select` iteration, a sub-flow invocation — creates a context scope. All scopes follow the same pattern: they inherit the full parent context by default (optionally restricted via `limit_to`), can create local scratch variables freely at runtime, and declare which keys merge back to the parent as `outputs`. Local variables die when the scope closes. This is the same recursion at every nesting level, with no special cases.

### 1.5 Unified Input/Output Contracts

The same input/output pattern appears at every level. The top-level process declares `input_schema` and `output_schema` (full JSON Schema). Definitions in the `definitions` section declare `inputs` and `outputs` (also full JSON Schema, since they're invoked externally and need validation). Individual nodes within a flow use simple key lists for `inputs` and `outputs`, since they operate within an already-defined schema. The pattern is one concept at varying levels of formality.

### 1.6 Universal Constraints

Constraints are a general-purpose node-level property, not specific to AI tasks. A REST call can have a `timeout_seconds` constraint just as an AI task can have a `cost_limit_usd` or `token_limit`. Guardrails are a sub-property of constraints that apply to AI-driven nodes. This avoids separate timeout, retry, and cost-limit mechanisms — one block handles all execution boundaries.

### 1.7 Everything is a Node

Tools, sub-processes, fragments, and reusable components are all defined the same way — as nodes or flows of nodes in the `definitions` section. A single-node definition is a "tool." A multi-node definition is a "sub-process." There is no structural distinction, no parallel taxonomy. The only difference is how many nodes a definition contains.

### 1.8 Self-Describing Nodes, Minimal Bindings

Nodes carry enough information to understand what they do without consulting bindings. A task node includes its service URL, method, and mappings. An LLM prompt node includes the model, template, and output schema. Bindings are reduced to platform service mappings — they answer "which platform handles this task type" and carry only platform-level configuration that the process author legitimately controls.

### 1.9 Definition vs. Instance

The process definition is a template with all dynamic possibilities in place — including the full set of selectable options, preconditions, and constraints. Each execution resolves into a concrete instance: a trace of which paths were taken, which options were selected, which tools were called. Versioning applies to definitions; instances are immutable records. There is no migration concern between definition versions — in-flight instances are already bound to their resolved paths.

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
  "bindings": { ... }
}
```

| Field | Required | Purpose |
|-------|----------|---------|
| `agent_flow` | yes | Specification version for forward compatibility |
| `metadata` | yes | Identity, description, determinism level |
| `client_id` | yes | Identity of this agent to all platform services |
| `input_schema` | yes | JSON Schema for what the process receives |
| `output_schema` | yes | JSON Schema for what the process produces |
| `nodes` | yes | Activities in the graph |
| `edges` | yes | Transitions including conditional routing |
| `definitions` | no | Named reusable components (sub-flows and single-step operations) |
| `bindings` | no | Platform component mappings (separable to own file) |

---

## 3. Metadata

```json
{
  "metadata": {
    "id": "order-fulfillment-v2",
    "name": "Order Fulfillment",
    "version": "2.1.0",
    "determinism": "hybrid",
    "tags": ["fulfillment", "hybrid"]
  }
}
```

| `determinism` | Meaning |
|---------------|---------|
| `"deterministic"` | All nodes use `fixed`/`none` mode. Traditional workflow equivalent. |
| `"hybrid"` | Mix of fixed and AI-driven nodes. |
| `"autonomous"` | Majority or all nodes use AI-driven modes. |

---

## 4. Client Identity

```json
{
  "client_id": "order-fulfillment-agent"
}
```

Identifies this agent to all platform services. Passed in every call the runtime makes. Used for authentication, authorization, rate limiting, audit trails, and cost attribution. Declared once at the top level.

---

## 5. Input and Output Schemas

The process declares what it receives and what it produces. Intermediate state (variables created and consumed internally) does not appear in either schema.

```json
{
  "input_schema": {
    "type": "object",
    "properties": {
      "order": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "customer_id": { "type": "string" },
          "items": { "type": "array" },
          "total": { "type": "number" }
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
│  │  ┌─ Sub-flow context (iteration 1) ─────────────┐  │  │
│  │  │  inherited: full parent (default)             │  │  │
│  │  │  locals: (created at runtime)                 │  │  │
│  │  │  outputs: [customer_reply]                    │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                    │  │
│  │  ┌─ Sub-flow context (iteration 2) ─────────────┐  │  │
│  │  │  inherited: full parent (default)             │  │  │
│  │  │  locals: (created at runtime)                 │  │  │
│  │  │  outputs: [resolution]                        │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### 6.2 Context Declaration

Full parent inheritance is the default. Declare a `context` block only when you need to restrict inheritance or declare outputs.

```json
"context": {
  "limit_to": ["order", "exception_notes"],
  "outputs": ["resolution"]
}
```

| Property | Default | Purpose |
|----------|---------|---------|
| `limit_to` | All parent keys (full inheritance) | Restricts which parent keys are visible in this scope |
| `outputs` | None | Keys that merge back to parent when scope closes |

Local scratch variables are created by steps at runtime. They exist within the scope and are discarded when it closes. They are never declared in the process definition.

### 6.3 Resolution Rules

1. On scope open: parent keys are available (all by default, or restricted by `limit_to`).
2. Nodes can read inherited keys and any locals created during execution.
3. Nodes write to locals freely and to declared output keys.
4. On scope close: `outputs` merge to parent state. Everything else is discarded.

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
| `task_type` | no | What kind of task (for `task` activity types) |
| `config` | no | Runtime-invariant parameters (URLs, templates, mappings) |
| `mode` | yes | Execution mode — how much autonomy the executor has |
| `inputs` | no | State keys read (key list for nodes, JSON Schema for definitions) |
| `outputs` | no | State keys written (key list for nodes, JSON Schema for definitions) |
| `context` | no | Scope configuration for select/subprocess modes |
| `constraints` | no | Timeouts, retries, cost/token limits, guardrails |
| `error_handlers` | no | Error routing for this node |
| `metadata` | no | Freeform annotations |

Any node can use `$ref` for external definitions:

```json
{ "id": "validate-order", "$ref": "./nodes/validate-order.json" }
```

### 7.2 Activity Types

| Activity Type | Semantics | Traditional Equivalent |
|---------------|-----------|----------------------|
| `"task"` | Unit of work producing output | Service Task / Script Task |
| `"user_interaction"` | Requires human input | User Task |
| `"llm_prompt"` | Invokes an LLM with a prompt template | *(none)* |
| `"decision"` | Selects an outgoing path (routing anchor) | Exclusive Gateway |
| `"parallel_split"` | Fans out to concurrent paths | Parallel Gateway (fork) |
| `"parallel_join"` | Waits for concurrent paths | Parallel Gateway (join) |
| `"subprocess"` | Invokes a sub-flow | Call Activity |
| `"event_wait"` | Pauses for external event | Intermediate Catch Event |
| `"emit_event"` | Publishes an event | Intermediate Throw Event |
| `"start"` | Entry point | Start Event |
| `"end"` | Terminal point | End Event |

### 7.3 Task Types

For `task` activity type nodes, `task_type` describes the kind of work. `config` carries all runtime-invariant parameters.

| Task Type | Purpose | Example Config |
|-----------|---------|---------------|
| `rest_call` | HTTP service invocation | `url`, `method`, `headers`, `input_mapping`, `output_mapping` |
| `grpc_call` | gRPC service invocation | `endpoint`, `service`, `method`, `input_mapping` |
| `notification` | Send a user-facing notification | `channel`, `template`, `recipient_mapping`, `params_mapping` |
| `event_publish` | Publish to an event bus | `topic`, `event_type`, `payload_mapping` |
| `script` | Run a deterministic script | `language`, `source` |
| `invoke_agent` | Call another agent definition | `agent_ref`, `input_mapping`, `output_mapping` |

Task types are extensible. New types can be added as platforms support new capabilities.

#### `rest_call` example

```json
{
  "activity_type": "task",
  "task_type": "rest_call",
  "config": {
    "url": "https://inventory.internal/api/validate",
    "method": "POST",
    "input_mapping": { "body.items": "$.order.items" },
    "output_mapping": { "validation_result": "$.response.result" }
  }
}
```

#### `notification` example

```json
{
  "activity_type": "task",
  "task_type": "notification",
  "config": {
    "channel": "customer-email",
    "template": "order-rejected",
    "recipient_mapping": "$.order.customer_email",
    "params_mapping": { "order_id": "$.order.id" }
  }
}
```

#### `event_publish` example

```json
{
  "activity_type": "task",
  "task_type": "event_publish",
  "config": {
    "topic": "manager-notifications",
    "event_type": "exception-review-requested",
    "payload_mapping": { "order_id": "$.order.id" }
  }
}
```

### 7.4 `llm_prompt` Activity Type

Nodes that invoke an LLM with a specific prompt template. The model is selected per task by the process author; the platform routes to the appropriate provider.

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
    "timeout_seconds": 86400
  },
  "mode": "fixed"
}
```

### 7.6 `user_interaction` Activity Type

```json
{
  "activity_type": "user_interaction",
  "config": {
    "form": "order-review-form",
    "instructions": "Review the flagged order and approve, reject, or escalate.",
    "input_mapping": { "order": "$.order", "notes": "$.exception_notes" },
    "output_mapping": { "decision": "$.review_decision" }
  },
  "mode": "fixed"
}
```

---

## 8. Execution Modes

### 8.1 `fixed` — No Autonomy

The node executes exactly what its `activity_type`, `task_type`, and `config` describe.

```json
"mode": "fixed"
```

### 8.2 `select` — AI Selects From Options

An LLM is presented with a set of eligible options and iteratively selects which to execute.

```json
"mode": {
  "type": "select",
  "instruction": "Resolve this customer exception...",
  "options": [
    { "ref": "lookup-order-history", "description": "Retrieves past orders." },
    {
      "ref": "auto-refund",
      "description": "Issues a refund automatically.",
      "precondition": { "expression": "state.get('order', {}).get('total', 0) < 100" }
    }
  ],
  "selection_mode": "iterative",
  "exit_condition": "resolution.action_taken is set",
  "max_iterations": 5
}
```

| Property | Required | Purpose |
|----------|----------|---------|
| `type` | yes | Always `"select"` |
| `instruction` | yes | System prompt for the selecting LLM |
| `options` | yes | Available choices (references to `definitions` entries) |
| `selection_mode` | yes | `"single"`, `"iterative"`, or `"parallel"` |
| `exit_condition` | no | Natural language or expression describing when to stop (for iterative) |
| `max_iterations` | no | Hard cap on iterations (for iterative/parallel) |

#### Selection Modes

| Mode | Behavior |
|------|----------|
| `"single"` | Pick one option, execute it, done. |
| `"iterative"` | Loop: pick → execute → inspect state → pick another or exit. Continues until `exit_condition` is met or `max_iterations` reached. |
| `"parallel"` | Pick multiple options, execute concurrently, merge results into context. |

#### Option Properties

| Property | Required | Purpose |
|----------|----------|---------|
| `ref` | yes | ID of a definition in the `definitions` section |
| `description` | yes | Shown to the LLM during selection |
| `precondition` | no | Deterministic filter; if false, option is excluded from the menu |

#### LLM Input Marshalling

At each selection step the LLM produces:

```json
{
  "option": "auto-refund",
  "input": { "order": { "...marshalled from current state..." } },
  "reasoning": "Low-value order, clear defect."
}
```

The runtime validates the `input` payload against the referenced definition's `inputs` schema.

### 8.3 `none` — Decision Nodes

Decision nodes are structural routing anchors. They perform no work. Routing logic lives on outbound edges.

```json
"mode": "none"
```

### 8.4 Mode Shape

`mode` is polymorphic: a string for `"fixed"` and `"none"` (which need no configuration), an object for `"select"` (which requires substantial configuration).

---

## 9. Edges

### 9.1 Basic Edges

```json
{ "from": "start", "to": "validate-order" }
```

### 9.2 Conditional Edges

Outbound edges from `decision` nodes carry conditions. The decision node is a structural anchor; the edges carry the logic.

```json
{
  "from": "route-validation",
  "to": "generate-label",
  "condition": { "expression": "state['validation_result'] == 'pass'" }
}
```

Default edge (taken if no other condition matches):

```json
{
  "from": "route-validation",
  "to": "handle-exception",
  "condition": { "default": true }
}
```

Edge conditions are evaluated using the process-level `expression_language` declared in bindings.

### 9.3 LLM-Driven Routing

For AI-driven path selection, the recommended pattern is: an `llm_prompt` node writes a routing value to state, then a deterministic decision node routes on it.

```json
{
  "id": "classify-exception",
  "activity_type": "llm_prompt",
  "config": {
    "model": "claude-sonnet-4-20250514",
    "template": "Classify this exception: escalate, refund, or investigate.\n\n{{exception_notes}}",
    "output_key": "exception_class"
  },
  "mode": "fixed",
  "outputs": ["exception_class"]
},
{
  "id": "route-exception",
  "activity_type": "decision",
  "mode": "none",
  "inputs": ["exception_class"]
}
```

With edges:

```json
{ "from": "classify-exception", "to": "route-exception" },
{
  "from": "route-exception", "to": "escalate-node",
  "condition": { "expression": "state['exception_class'] == 'escalate'" }
},
{
  "from": "route-exception", "to": "refund-node",
  "condition": { "expression": "state['exception_class'] == 'refund'" }
},
{
  "from": "route-exception", "to": "investigate-node",
  "condition": { "default": true }
}
```

This keeps the AI's decision captured in state and fully inspectable in the trace.

### 9.4 Edge Properties

| Field | Required | Purpose |
|-------|----------|---------|
| `from` | yes | Source node ID |
| `to` | yes | Target node ID |
| `condition` | no | Routing condition (for decision node outbound edges) |
| `metadata` | no | Annotations |

---

## 10. Definitions

Named, reusable components referenced by `select` options, `subprocess` nodes, or `$ref`. Every definition has the same structure: an interface contract (`inputs`/`outputs`) and a flow (`nodes`/`edges`).

A single-node definition is a "tool." A multi-node definition is a "sub-process." Both are structurally identical.

### 10.1 Definition Structure

```json
{
  "definitions": {
    "definition-id": {
      "description": "Human-readable description",
      "inputs": { "type": "object", "properties": { ... } },
      "outputs": { "type": "object", "properties": { ... } },
      "context": { ... },
      "nodes": [ ... ],
      "edges": [ ... ]
    }
  }
}
```

| Field | Required | Purpose |
|-------|----------|---------|
| `description` | yes | Shown to LLMs during selection and used for documentation |
| `inputs` | yes | JSON Schema for the definition's input contract |
| `outputs` | yes | JSON Schema for the definition's output contract |
| `context` | no | Scope configuration (limit_to, outputs) |
| `nodes` | yes | Activities within the definition |
| `edges` | yes | Transitions within the definition |

Internal edges use `__start__` and `__end__` as entry/exit pseudo-nodes.

### 10.2 External References

Any definition can be an external file:

```json
{
  "definitions": {
    "escalate-to-manager": { "$ref": "./definitions/escalate-to-manager.json" },
    "lookup-order-history": { "$ref": "https://definitions.internal/crm/lookup-history.json" }
  }
}
```

`$ref` also works on individual nodes:

```json
{ "id": "validate-order", "$ref": "./nodes/validate-order.json" }
```

The `id` is always local to the containing flow; the `$ref` provides the body.

---

## 11. Constraints

A general-purpose node-level property for execution boundaries. Applies to any node type.

```json
"constraints": {
  "timeout_seconds": 300,
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

| Property | Applies To | Purpose |
|----------|-----------|---------|
| `timeout_seconds` | Any node | Maximum execution time |
| `max_retries` | Any node | Retry attempts on transient failure |
| `retry_backoff_seconds` | Any node | Backoff schedule for retries |
| `cost_limit_usd` | AI-driven nodes | Maximum spend on LLM calls |
| `token_limit` | AI-driven nodes | Maximum token consumption across LLM calls |
| `guardrails` | AI-driven nodes | Content filters and custom validation rules |

Guardrail types:

| Type | Purpose |
|------|---------|
| `content_filter` | Built-in content safety checks (PII, toxicity, etc.) |
| `custom` | User-defined expression evaluated against state |

---

## 12. Error Handlers

Error types are defined by the platform, not by the process. The process file contains only per-node error handlers that reference the platform's error taxonomy.

```json
"error_handlers": [
  { "error_type": "service_error", "action": "retry" },
  { "error_type": "timeout_error", "action": "route", "target": "handle-exception" },
  { "error_type": "guardrail_violation", "action": "retry", "max_attempts": 2 },
  { "error_type": "cost_limit_exceeded", "action": "route", "target": "reject-order" },
  { "error_type": "no_selection_reached", "action": "route", "target": "handle-exception" }
]
```

### 12.1 Standard Error Types (Platform-Defined)

These are the standard error types that platforms are expected to support:

| Error Type | Trigger | Retryable |
|------------|---------|-----------|
| `service_error` | External service call failed (4xx, 5xx, timeout, connection) | yes |
| `validation_error` | Node output failed schema validation | no |
| `timeout_error` | Node exceeded `timeout_seconds` constraint | yes |
| `guardrail_violation` | Constraint or guardrail check failed | conditional |
| `cost_limit_exceeded` | AI node exceeded `cost_limit_usd` | no |
| `token_limit_exceeded` | AI node exceeded `token_limit` | no |
| `no_selection_reached` | `select` mode hit `max_iterations` without meeting exit condition | no |
| `internal_error` | Unexpected runtime exception | no |

### 12.2 Handler Actions

| Action | Behavior |
|--------|----------|
| `"retry"` | Re-execute using the node's `constraints` retry config |
| `"route"` | Redirect to a specified node |
| `"abort"` | Terminate agent execution |
| `"emit_event"` | Publish an error event via the platform event bus |

When `"action": "retry"` is specified and `max_attempts` is included on the handler, it overrides the node's `constraints.max_retries` for that specific error type.

---

## 13. Bindings

Bindings map task types to platform components. They answer two questions: what runtime orchestrates this agent, and which platform service handles each task type.

### 13.1 Structure

```json
{
  "bindings": {
    "runtime": "langgraph",
    "runtime_version": "1.0.0",
    "expression_language": "python",

    "components": {
      "task_type": {
        "platform": "platform_name",
        "target_version": "x.y.z",
        "service": "service_identifier",
        "config": { }
      }
    }
  }
}
```

| Field | Required | Purpose |
|-------|----------|---------|
| `runtime` | yes | Orchestration runtime |
| `runtime_version` | yes | Abstracted version of the orchestrator |
| `expression_language` | yes | Language for inline expressions (edge conditions, preconditions, guardrails) |
| `components` | yes | Map of task types to platform services |

### 13.2 Component Properties

| Property | Required | Purpose |
|----------|----------|---------|
| `platform` | yes | Target execution platform |
| `target_version` | yes | Abstracted version of the target platform |
| `service` | yes | Service identifier within the platform |
| `config` | yes | Business-logic parameters (can be empty) |

### 13.3 What Goes Where

| Detail | Location | Rationale |
|--------|----------|-----------|
| Service URL, method, path | Node `config` | Property of the service being called |
| Event topic, event type | Node `config` | Property of the event architecture |
| Prompt template, model selection | Node `config` | Task-level design choices |
| Which platform handles a task type | Binding `components` | Deployment decision |
| Platform service identity | Binding `components.service` | Platform routing |
| Default sender address, priority tier | Binding `components.config` | Platform-level business defaults |
| Auth, transport, proxies, connection pools | Platform-internal | Not exposed to process authors |

### 13.4 Expression Language Scope

The `expression_language` at the top level governs **inline expressions in the process model** — edge conditions, preconditions, guardrail expressions. These are evaluated by the orchestrator.

Script execution and expression evaluation *inside* a platform component is that platform's concern. A Camunda component may internally use FEEL or Groovy; the AI platform may use Python for scripts. This does not affect the process model's expression language.

### 13.5 Platform Versions

`target_version` values are abstracted platform versions, not raw software release numbers. Version `1.0.0` of a platform might internally run different software versions over time. The platform team manages backward compatibility within a version and provides documented migration paths between versions.

---

## 14. Runtime Mapping Reference

### 14.1 LangGraph Orchestrator

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
| `$ref` | Loaded at compile time |

### 14.2 Traditional Workflow Engine (e.g., Camunda)

| Spec Concept | Camunda Equivalent |
|---|---|
| Process | Process Definition |
| `task` + `fixed` | Service Task / External Task |
| `user_interaction` + `fixed` | User Task |
| `decision` + conditional edges | Exclusive Gateway + Sequence Flows |
| `parallel_split` / `parallel_join` | Parallel Gateway |
| `event_wait` | Intermediate Message Catch Event |
| Definition (multi-node) | Embedded Sub-Process |
| `select` mode | **No equivalent** (flagged on deployment validation) |
| `llm_prompt` | **No equivalent** (flagged on deployment validation) |

---

## 15. Full Example: Hybrid Order Fulfillment

```json
{
  "agent_flow": "1.0.0",

  "metadata": {
    "id": "order-fulfillment-v2",
    "name": "Order Fulfillment",
    "version": "2.1.0",
    "determinism": "hybrid"
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
          "total": { "type": "number" }
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
        "timeout_seconds": 30,
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
        "timeout_seconds": 60
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
        "timeout_seconds": 600,
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
    { "from": "start", "to": "validate-order" },
    { "from": "validate-order", "to": "route-validation" },
    {
      "from": "route-validation",
      "to": "generate-label",
      "condition": { "expression": "state['validation_result'] == 'pass'" }
    },
    {
      "from": "route-validation",
      "to": "reject-order",
      "condition": { "expression": "state['validation_result'] == 'fail'" }
    },
    {
      "from": "route-validation",
      "to": "handle-exception",
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
        "properties": {
          "order": { "type": "object" },
          "exception_notes": { "type": "string" }
        },
        "required": ["order"]
      },
      "outputs": {
        "type": "object",
        "properties": { "resolution": { "type": "object" } }
      },
      "nodes": [
        {
          "id": "notify-manager",
          "name": "Notify Manager",
          "activity_type": "task",
          "task_type": "event_publish",
          "config": {
            "topic": "manager-notifications",
            "event_type": "exception-review-requested",
            "payload_mapping": {
              "order_id": "$.order.id",
              "exception": "$.exception_notes"
            }
          },
          "mode": "fixed",
          "inputs": ["order", "exception_notes"]
        },
        {
          "id": "await-decision",
          "name": "Await Manager Decision",
          "activity_type": "event_wait",
          "config": {
            "event_type": "manager-decision",
            "correlation_key": "$.order.id"
          },
          "mode": "fixed",
          "outputs": ["resolution"]
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
          "id": "issue-refund",
          "name": "Issue Refund",
          "activity_type": "task",
          "task_type": "rest_call",
          "config": {
            "url": "https://payments.internal/api/refund",
            "method": "POST",
            "input_mapping": {
              "body.amount": "$.order.total",
              "body.order_id": "$.order.id"
            },
            "output_mapping": {
              "resolution.action_taken": "'auto_refund'",
              "resolution.refund_amount": "$.response.amount"
            }
          },
          "mode": "fixed",
          "inputs": ["order"],
          "outputs": ["resolution"]
        },
        {
          "id": "send-confirmation",
          "name": "Send Confirmation",
          "activity_type": "task",
          "task_type": "notification",
          "config": {
            "channel": "customer-email",
            "template": "refund-issued",
            "recipient_mapping": "$.order.customer_email",
            "params_mapping": {
              "order_id": "$.order.id",
              "refund_amount": "$.resolution.refund_amount"
            }
          },
          "mode": "fixed",
          "inputs": ["order", "resolution"]
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
        "properties": {
          "order": { "type": "object" },
          "exception_notes": { "type": "string" }
        },
        "required": ["order", "exception_notes"]
      },
      "outputs": {
        "type": "object",
        "properties": { "customer_reply": { "type": "string" } }
      },
      "nodes": [
        {
          "id": "draft-question",
          "name": "Draft Clarification",
          "activity_type": "llm_prompt",
          "config": {
            "model": "claude-sonnet-4-20250514",
            "template": "Based on the following order and exception, draft a clear, polite question to the customer asking for the specific information needed.\n\nOrder: {{order}}\nException: {{exception_notes}}\n\nDraft a brief email body only.",
            "input_mapping": {
              "order": "$.order",
              "exception_notes": "$.exception_notes"
            },
            "output_key": "draft_message"
          },
          "mode": "fixed",
          "inputs": ["order", "exception_notes"],
          "outputs": ["draft_message"]
        },
        {
          "id": "send-question",
          "name": "Send to Customer",
          "activity_type": "task",
          "task_type": "notification",
          "config": {
            "channel": "customer-email",
            "template": "clarification-question",
            "recipient_mapping": "$.order.customer_email",
            "params_mapping": { "body": "$.draft_message" }
          },
          "mode": "fixed",
          "inputs": ["order", "draft_message"]
        },
        {
          "id": "await-reply",
          "name": "Await Customer Reply",
          "activity_type": "event_wait",
          "config": {
            "event_type": "customer-reply",
            "correlation_key": "$.order.id",
            "timeout_seconds": 86400
          },
          "mode": "fixed",
          "outputs": ["customer_reply"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "draft-question" },
        { "from": "draft-question", "to": "send-question" },
        { "from": "send-question", "to": "await-reply" },
        { "from": "await-reply", "to": "__end__" }
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
        "properties": {
          "order_history": { "type": "array", "items": { "type": "object" } }
        }
      },
      "nodes": [
        {
          "id": "lookup",
          "name": "Lookup Order History",
          "activity_type": "task",
          "task_type": "rest_call",
          "config": {
            "url": "https://crm.internal/api/orders",
            "method": "GET",
            "input_mapping": { "query.customer_id": "$.customer_id" },
            "output_mapping": { "order_history": "$.response.orders" }
          },
          "mode": "fixed",
          "inputs": ["customer_id"],
          "outputs": ["order_history"]
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
        "properties": {
          "stock_levels": { "type": "object", "additionalProperties": { "type": "integer" } }
        }
      },
      "nodes": [
        {
          "id": "check",
          "name": "Check Inventory",
          "activity_type": "task",
          "task_type": "rest_call",
          "config": {
            "url": "https://inventory.internal/api/stock",
            "method": "POST",
            "input_mapping": { "body.skus": "$.skus" },
            "output_mapping": { "stock_levels": "$.response.levels" }
          },
          "mode": "fixed",
          "inputs": ["skus"],
          "outputs": ["stock_levels"]
        }
      ],
      "edges": [
        { "from": "__start__", "to": "check" },
        { "from": "check", "to": "__end__" }
      ]
    }
  },

  "bindings": {
    "runtime": "langgraph",
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
        "config": {
          "default_from": "noreply@notifications.internal"
        }
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
        "config": {}
      }
    }
  }
}
```

---

## 16. Open Questions

### 16.1 Config Schema Per Task Type

Should the spec define a formal JSON Schema for each `task_type`'s `config` block (e.g., `rest_call` config must have `url` and `method`)? This aids tooling and validation but adds maintenance as task types grow.

### 16.2 Environment-Specific Config

Service URLs in node config may differ between environments. A generic value interpolation mechanism (e.g., `"url": "{{INVENTORY_BASE_URL}}/api/validate"` resolved from state or environment) would address this without adding environment-specific overlays to the spec.

### 16.3 Shared Definition Libraries

`$ref` enables external definitions, but doesn't address packaging (how to distribute a library of definitions), discovery (how to find available definitions), or compatibility (how to verify a definition works with a given input schema). A manifest format for definition libraries may be needed.

### 16.4 Camunda Task-Level Execution

Camunda natively runs processes, not individual tasks. In hybrid mode, the orchestrator needs to invoke Camunda for specific task execution. This requires Camunda to expose task-level execution — via external task workers, job workers, or an API layer. This is an architecture concern for the platform team, not a modeling concern.

### 16.5 Trace Schema

Execution traces — the resolved instance records of which paths were taken, options selected, tools called, and errors encountered — need a standardized format. This should be specified as a separate companion spec, versioned independently. Platforms produce trace entries; the orchestrator consolidates them.
