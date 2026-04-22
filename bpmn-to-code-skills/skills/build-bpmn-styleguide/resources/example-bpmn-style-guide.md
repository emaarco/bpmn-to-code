# BPMN Style Guide

Engine: CAMUNDA_7
Language: en

This guide captures how our team models BPMN processes: the naming conventions we follow, the BPMN elements we use (and when to reach for each), the technical configuration we require, and the engine-specific special cases we've learned to handle. Read it end-to-end when you start modelling a new process; skim it for a refresher when reviewing one.

The guide is divided into three parts:

1. **🎨 Business Modeling** — how a model should read to a human.
2. **🔧 Technical Configuration** — IDs, topics, engine-level settings.
3. **⚠️ Special Cases** — patterns the engine forces on us.

Each rule block is annotated with a machine-readable YAML stanza so tooling can enforce it automatically. The annotations live inside HTML comments (`<!-- rule:slug -->`) and fenced code blocks — they are invisible when the guide is rendered but survive in source. Two companion skills consume them:

- `/validate-bpmn-style` — lints live BPMN files against this guide (advisory, not a CI gate).
- `/generate-rules-to-enforce-bpmn-styleguide` — generates Kotlin `BpmnValidationRule` implementations for the deterministic rules, usable with `bpmn-to-code-testing`.

---

## 🎨 Business Modeling

Readable diagrams start here. These rules are about naming, layout, and choosing the right BPMN element for the job. Most of them require human judgement and are enforced by `/validate-bpmn-style`; a handful also have a deterministic shape.

### Naming — Tasks

Describe **what has to be done**. One noun + one verb is usually enough. Keep it in the present tense; the task is an action, not a state.

- good: `Send confirmation mail`, `Validate address`, `Check inventory`
- bad: `Email`, `Confirmation sent`, `Do stuff`, `Check`

<!-- rule:task-naming -->
```yaml
category: business-modeling
severity: warning
applies-to: [SERVICE_TASK, USER_TASK, SEND_TASK, RECEIVE_TASK, SCRIPT_TASK, MANUAL_TASK, BUSINESS_RULE_TASK, TASK]
validation: llm
```

### Naming — Events

Describe **what has happened**. Use a noun + a passive/past-tense verb. The event captures a state change; the name reflects that state.

- good: `Order received`, `Payment authorized`, `Registration aborted`
- bad: `Receive order`, `Authorize payment`, `Abort registration`

<!-- rule:event-naming -->
```yaml
category: business-modeling
severity: warning
applies-to: [START_EVENT, END_EVENT, INTERMEDIATE_CATCH_EVENT, INTERMEDIATE_THROW_EVENT, BOUNDARY_EVENT]
validation: llm
```

### Naming — Gateways

Phrase the decision as a **short question**. One noun + one verb when you can.

- good: `Customer known?`, `Inventory sufficient?`
- bad: `Check customer`, `Gateway_0a1b2c3`

<!-- rule:gateway-naming -->
```yaml
category: business-modeling
severity: warning
applies-to: [EXCLUSIVE_GATEWAY, INCLUSIVE_GATEWAY]
validation: llm
```

### Naming — Pools and Lanes

Use speaking names for the actor or system that owns the work: `Customer system`, `User`, `Fulfilment service`. Avoid generic labels like `Lane 1`.

<!-- rule:pool-lane-naming -->
```yaml
category: business-modeling
severity: info
applies-to: [all-flow-nodes]
validation: llm
```

### Allowed Elements

We don't restrict BPMN — any element is allowed. These are the defaults we reach for; deviate only when you have a reason.

- **User Task** — a specific person needs to act on the task (e.g. enter a best-before date for a scanned article).
- **Message Catch Event** — we wait for an external input, typically a Kafka message.
- **Receive Task** — like a message catch event, but we need to react *while* waiting (e.g. the order can be cancelled during the confirmation wait).
- **Send Task vs. Service Task + outgoing message** — use a send task when the whole purpose of the task is to emit a message.
- **Business Rule Task** — only when the logic is genuinely a DMN decision table. Don't overload it with integration logic.
- **Script Task** — avoid in production flows. Use a service task instead so the behaviour is testable.
- **Call Activity** — when the sub-process stands on its own and has a lifecycle (owned BPMN file, its own process ID, reusable).

<!-- rule:allowed-notation-choices -->
```yaml
category: business-modeling
severity: info
applies-to: [all-flow-nodes]
validation: deterministic
```
*Deterministic: set-membership check against `FlowNodeDefinition.elementType`. An element whose type is not on the allowed list is a violation. The list above is the source of truth.*

### Layout

- **Reading direction**: left to right. The start event sits on the left, end events on the right.
- **Spacing**: leave enough room between elements that labels don't collide.
- **No crossings**: if sequence flows would cross, break the flow with an intermediate event, a sub-process, or a layout change. Crossings are a smell that the process is hiding complexity.

<!-- rule:layout-left-to-right -->
```yaml
category: business-modeling
severity: info
applies-to: [process]
validation: llm
```

---

## 🔧 Technical Configuration

These rules govern how automation-relevant elements are identified and configured.
They're mostly deterministic and can be enforced by either the LLM via `/validate-bpmn-style` or by the generated `BpmnValidationRule`s in your test suite.

### Element IDs

Every element that matters for automation has an ID in the format `type_DescriptionInCamelCase`. The `type_` prefix mirrors the BPMN element type; the description is PascalCase.

| Element | Example |
|---|---|
| Start Event | `startEvent_OrderReceived` |
| Intermediate Event | `event_ShipmentDelayed` |
| End Event | `endEvent_OrderCompleted` |
| Service Task | `serviceTask_SendConfirmationMail` |
| User Task | `userTask_ApproveOrder` |
| Receive Task | `receiveTask_AwaitPayment` |
| Gateway | `gateway_CustomerKnown` |
| Sub-Process | `subProcess_Confirmation` |
| Call Activity | `callActivity_AbortRegistration` |
| Timer Event | `timer_After3Days` |

The auto-generated IDs from the Camunda Modeler (`Activity_0x7f3a`, `Gateway_1abc`) are rejected — rename them before you commit.

<!-- rule:element-id-format -->
```yaml
category: technical-configuration
severity: error
applies-to: [all-flow-nodes]
validation: deterministic
pattern: "^(startEvent|endEvent|event|serviceTask|userTask|receiveTask|sendTask|scriptTask|manualTask|businessRuleTask|gateway|subProcess|callActivity|task|timer|boundaryEvent)_[A-Z][a-zA-Z0-9]*$"
```

### Process IDs

Process IDs are kebab-case, no `process_` prefix, no version suffix. The name has a domain meaning (the business capability), not a technical one.

- good: `newsletter-subscription`, `order-fulfillment`, `invoice-processing`
- bad: `process_NewsletterSubscription`, `newsletterSubscriptionV2`, `PROC_01`

<!-- rule:process-id-format -->
```yaml
category: technical-configuration
severity: error
applies-to: [process]
validation: deterministic
pattern: "^[a-z][a-z0-9]*(-[a-z0-9]+)*$"
```

### Message IDs

Messages are identified service-wide by `<serviceName>.<myState>` in camelCase. The service name is the bounded context; the state is what's being communicated.

- good: `order.orderCreated`, `newsletter.subscriptionConfirmed`
- bad: `Message_ConfirmSubscription`, `newsletter_subscription_confirmed`

<!-- rule:message-id-schema -->
```yaml
category: technical-configuration
severity: error
applies-to: [messages]
validation: hybrid
pattern: "^[a-z][a-zA-Z0-9]*\\.[a-z][a-zA-Z0-9]*$"
```
*Hybrid: the regex catches the shape. `/validate-bpmn-style` additionally checks that `<serviceName>` matches the bounded context of the process.*

### Service Task Topics (Type IDs)

A Camunda 7 topic (or Zeebe job type) is the identifier the worker subscribes to. We use `<serviceName>.<elementIdWithoutPrefix>` in camelCase — same `<serviceName>` as the messages, so topics and messages line up.

- good: `order.validateAddress`, `newsletter.sendConfirmationMail`
- bad: `SendConfirmationMail`, `newsletter_send_confirmation_mail`

<!-- rule:service-task-topic -->
```yaml
category: technical-configuration
severity: error
applies-to: [SERVICE_TASK]
validation: hybrid
pattern: "^[a-z][a-zA-Z0-9]*\\.[a-z][a-zA-Z0-9]*$"
```
*Hybrid: regex catches the shape; the AI linter additionally checks that `<elementIdWithoutPrefix>` actually matches the element ID (minus `serviceTask_` prefix, lowercased).*

### Gateway Flow Labels

Outgoing sequence flows from exclusive or inclusive gateways are labelled. Labels should answer the question asked at the gateway (`yes` / `no` or a short descriptor).

<!-- rule:gateway-flow-labels -->
```yaml
category: technical-configuration
severity: error
applies-to: [sequence-flows]
validation: deterministic
```

### Process Variables

Keep process variables minimal. The engine is not a database — pass only the data needed for orchestration decisions. A flag `isPremiumCustomer` is fine; the whole customer object as JSON is too much. When in doubt, load from the domain service inside the worker.

<!-- rule:minimize-process-variables -->
```yaml
category: technical-configuration
severity: info
applies-to: [process]
validation: llm
```

---

## ⚠️ Special Cases

Patterns the engine forces on us. Skim once; revisit when they bite.

### Transaction Boundaries (Async Continuations, C7 / Operaton / CIB-7)

CIB-7 is an **embedded engine** — the process runtime shares the JVM and the database transaction with the service. Without explicit transaction boundaries, a failure in a late task can roll back earlier tasks whose business effects have already been persisted. The conventions below keep engine state and business state aligned.

TL;DR:

| Element | Convention |
|---|---|
| Service Task (`type=external`) | **never** `asyncBefore` / `asyncAfter`. The external-task worker already acts as a transaction boundary. Other implementation kinds (`JAVA_DELEGATE`, `DELEGATE_EXPRESSION`, `EXPRESSION`) are not covered by this default — decide per deployment model. |
| Start Event | `asyncAfter=true`. |
| User Task | `asyncAfter=true`. |
| Boundary Event (all types) | `asyncAfter=true`. |
| Intermediate Catch Event (all types) | `asyncAfter=true`. |

<!-- rule:async-continuations -->
```yaml
category: technical-configuration
severity: error
applies-to: [SERVICE_TASK, USER_TASK, START_EVENT, BOUNDARY_EVENT, INTERMEDIATE_CATCH_EVENT]
engine: [CAMUNDA_7, OPERATON]
validation: deterministic
```
*Deterministic: per-element attribute check — external service tasks must have neither `asyncBefore` nor `asyncAfter`; start / user / boundary / intermediate-catch events must have `asyncAfter=true`.*

### Message Correlation (Zeebe)

If this style guide targets Zeebe, every message definition must carry a `zeebe:subscription` element with a FEEL `correlationKey` expression. This isn't optional — Zeebe can't correlate messages without it.

*(Not applicable to this C7 example, but included here so a Zeebe-targeted guide has a template.)*

---

## Validation Reference

| Field | Values | Meaning |
|---|---|---|
| `category` | `business-modeling` \| `technical-configuration` | Which section of this handbook the rule belongs to |
| `severity` | `error` \| `warning` \| `info` | Reporting level |
| `applies-to` | BPMN element types or meta-categories (`all-flow-nodes`, `sequence-flows`, `process`, `messages`, `signals`, `errors`, `escalations`) | What the rule checks |
| `validation` | `deterministic` \| `llm` \| `hybrid` | How the rule gets enforced |
| `pattern` | regex | Deterministic check (required when `validation` includes a deterministic half) |
| `engine` | subset of `[CAMUNDA_7, OPERATON, CIB_7, ZEEBE]` | Optional engine scope; defaults to the top-level `Engine:` |

`validation: hybrid` means both halves exist. `/generate-rules-to-enforce-bpmn-styleguide` generates Kotlin for the deterministic half; `/validate-bpmn-style` runs both halves and quotes the rule's prose in the violation message.
