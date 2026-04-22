# Camunda 7 (C7) — Supported BPMN Elements and Configuration

This file documents the subset of BPMN that Camunda 7 supports and the engine-specific configuration attributes a style guide can target. Use it to decide which questions to ask the user and which conventions to encode in `BPMN_STYLE_GUIDE.md`.

XML namespace: `xmlns:camunda="http://camunda.org/schema/1.0/bpmn"`

## Supported Elements

| Element | Supported | Notes |
|---|---|---|
| Service Task | yes | Four implementation kinds — see below |
| User Task | yes | Supports assignee/candidateUsers/candidateGroups extensions |
| Receive Task | yes | Correlates to a message definition |
| Send Task | yes | Implementation configuration like service task |
| Script Task | yes | Inline script or external resource |
| Manual Task | yes | No automation |
| Business Rule Task | yes | DMN integration via `camunda:decisionRef` |
| Task (generic) | yes | No automation |
| Start Event | yes | None / Message / Signal / Timer / Error |
| End Event | yes | None / Message / Signal / Terminate / Error / Escalation / Compensation |
| Intermediate Catch Event | yes | Message / Signal / Timer / Conditional / Link |
| Intermediate Throw Event | yes | None / Message / Signal / Compensation / Escalation / Link |
| Boundary Event | yes | Interrupting + non-interrupting; Message / Signal / Timer / Error / Escalation / Compensation / Conditional |
| Exclusive Gateway | yes | `default` attribute for default flow |
| Parallel Gateway | yes | — |
| Inclusive Gateway | yes | `default` attribute for default flow |
| Event-Based Gateway | yes | Only connects to intermediate catch events |
| Complex Gateway | yes (rare) | — |
| Sub-Process | yes | Embedded + Event Sub-Process + Transaction |
| Event Sub-Process | yes | `triggeredByEvent="true"` |
| Call Activity | yes | `calledElement` attribute |
| Transaction | yes | — |
| Escalation | yes | Supported as event definition |

## Service Task Implementation Kinds

Exactly one of the following attributes is set on `<bpmn:serviceTask>`. The `implementationValue` is the attribute's value; the `implementationKind` is the enum name below.

| Kind | Attribute | Typical value shape |
|---|---|---|
| `EXTERNAL_TASK` | `camunda:type="external" camunda:topic="..."` | dot.separated.topic, e.g. `newsletter.sendConfirmationMail` |
| `JAVA_DELEGATE` | `camunda:class="..."` | Fully qualified class name |
| `DELEGATE_EXPRESSION` | `camunda:delegateExpression="${bean}"` | EL expression (usually a Spring bean name) |
| `EXPRESSION` | `camunda:expression="${...}"` | EL expression |

Source: `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/extractor/Camunda7ImplementationKind.kt`

Style guide implications:
- Teams typically pick one kind project-wide (e.g., external tasks for worker-based projects, delegate expressions for monolithic Spring deployments). A rule can enforce that choice.
- External-task topics benefit from a schema rule (e.g., `<serviceName>.<elementIdWithoutPrefix>`).

## Transaction Boundaries — Async Continuation Best Practices

Camunda 7 and its forks run as an **embedded engine**: the process runtime shares the JVM and the database transaction with the surrounding service. Without explicit transaction boundaries, a failure in a late task can roll back earlier tasks whose business effects were already persisted. Async continuations (`asyncBefore`, `asyncAfter`, `exclusive`) define those boundaries.

The best-practice default shipped with this skill follows the rules below. `build-bpmn-styleguide` Phase 4 compares what the user's models actually do against this default and lets them adopt, override, or skip.

| Element | Default convention | Rationale |
|---|---|---|
| Service Task (`type=external`) | **never** `asyncBefore` / `asyncAfter`. | External-task workers poll from the engine — the handover is already a transaction boundary. Adding async on top creates a redundant job and confuses error handling. For other service-task kinds (`JAVA_DELEGATE`, `DELEGATE_EXPRESSION`, `EXPRESSION`) the skill does not propose a default — pick one consciously per your deployment model. |
| Start Event | `asyncAfter=true` | Keeps the process-start commit separate from the first task. A failure in the first task doesn't roll back the process instance. |
| User Task | `asyncAfter=true` | A user task is a wait state. Post-completion logic belongs in its own transaction so a failure doesn't make the user redo the task. |
| Boundary Event (message / timer / signal / error) | `asyncAfter=true` | The boundary event commits the moment it fires; subsequent logic runs in a fresh transaction. |
| Intermediate Catch Event (message / timer / signal) | `asyncAfter=true` | Same reasoning as boundary events — the catch is the wait state. |

**Sources and further reading:**
- Camunda 7 docs: [Transactions in Processes](https://docs.camunda.org/manual/latest/user-guide/process-engine/transactions-in-processes/)
- The `asyncBefore` / `asyncAfter` / `exclusive` attributes are documented in the next section of this file.

### What Phase 4 of `build-bpmn-styleguide` should do

1. Parse each BPMN file the user already has. For each element type in the table above, count how many elements match / violate the default.
2. Present the findings via `AskUserQuestion`. Offer four choices:
   - **Adopt the default** — emit the `async-continuations` rule exactly as in the table.
   - **Use detected conventions** — emit the rule with the team's current patterns baked in.
   - **Customize** — walk through each element type and confirm each default.
   - **Skip** — omit the rule entirely.
3. Whichever option wins, emit one `<!-- rule:async-continuations -->` block in the Special Cases section with `validation: deterministic` — the rule is a per-element attribute check.

## Async / Execution Attributes (async triple)

Available on any activity / event:

| Attribute | Default | Purpose |
|---|---|---|
| `camunda:asyncBefore="true"` | `false` | Transaction boundary before the activity |
| `camunda:asyncAfter="true"` | `false` | Transaction boundary after the activity |
| `camunda:exclusive="false"` | `true` | Allow concurrent execution of the same process instance |

Style guide implications:
- Some teams require explicit `asyncBefore` on message/signal start events of event sub-processes.
- Others require `exclusive="true"` (the default) as a reminder.

## Variables

Captured by the extractor:
- `camunda:inputParameter` / `camunda:outputParameter` on activities
- `camunda:in` / `camunda:out` on call activities
- `camunda:elementVariable` + `camunda:collection` on multi-instance activities
- `camunda:properties` with a property named `additionalVariables` (whitespace-separated list) for variables touched outside the BPMN XML

## Messages, Signals, Errors, Escalations

- Messages and signals are top-level definitions referenced by event definitions (`messageRef`, `signalRef`).
- Errors have both `name` and `errorCode`. A style rule can enforce an error-code pattern.
- Escalations have `name` and `escalationCode`. Supported on end events, throw events, boundary events.

## Timers

Inside `<bpmn:timerEventDefinition>`, exactly one of:
- `<bpmn:timeDate>ISO-8601</bpmn:timeDate>`
- `<bpmn:timeDuration>ISO-8601</bpmn:timeDuration>` (e.g. `PT5M`, `PT1D`)
- `<bpmn:timeCycle>R5/PT1H</bpmn:timeCycle>` or a cron expression

Expressions may be used (`${expr}`), resolved at runtime.

## Example XML snippets

```xml
<bpmn:serviceTask id="serviceTask_SendEmail"
                  name="Send email"
                  camunda:type="external"
                  camunda:topic="newsletter.sendEmail"
                  camunda:asyncBefore="true">
  <bpmn:extensionElements>
    <camunda:inputOutput>
      <camunda:inputParameter name="recipient">${email}</camunda:inputParameter>
    </camunda:inputOutput>
  </bpmn:extensionElements>
</bpmn:serviceTask>

<bpmn:userTask id="userTask_Approve"
               name="Approve order"
               camunda:assignee="${approver}" />

<bpmn:callActivity id="callActivity_VerifyPayment"
                   calledElement="payment-verification" />

<bpmn:intermediateCatchEvent id="event_WaitForConfirmation">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration>PT5M</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:intermediateCatchEvent>
```
