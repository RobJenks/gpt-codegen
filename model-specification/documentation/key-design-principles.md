# Key design principles
The **Agent Flow** specification proposes the following key design principles
1. **Hybrid execution** 
   * One process definition can target multiple platforms simultaneously; deterministic tasks on traditional engines, AI tasks on an AI platform, coordinated by a single orchestrator executing processses specified in Google ADK or LangGraph.  
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
   * Elements within a fully-specified data contract (e.g. individual nodes within a process that has defined input schema) can alternatively refer to data keys for simplicity and to avoid duplication/model fragility.  E.g. "$.order.id" where the parent process alrady contains an "order" object with "id" property
6. **Constraints and guardrails**
   * Process steps can be controlled with a number of constraints
     * Agent ability to dynamically select actions is limited by the set of sub-flows/tools provided to it, the data scope it is allowed to access, and other restrictions such as the number of iterations it is allowed to reach a conclusion
     * All nodes also have a "constraints" property to limit factors such as execution time, retry limits, cost or token limits, and custom guardrails.  These are platform-agnostic, e.g. a timeout constraint will apply to both LLM response generation and service call response time.  Some constraints are not relevant for certain node types
     * Custom guardrails can be defined using the expression language of the orchestrator platform, for example ensuring that `$.order.refundAmount <= $.order.purchaseAmount`at every step in agent execution, otherwise a "guardrail breached" exception will be thrown
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

