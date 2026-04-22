# Zeebe (Camunda 8) — Supported BPMN Elements and Configuration

Zeebe is Camunda's cloud-native engine. Its BPMN subset and configuration model differ significantly from Camunda 7.

XML namespace: `xmlns:zeebe="http://camunda.org/schema/zeebe/1.0"`

## Supported Elements

| Element | Supported | Notes |
|---|---|---|
| Service Task | yes | Job-worker only — see below |
| User Task | yes | Tasklist integration |
| Receive Task | yes | Message correlation required |
| Send Task | yes | Like service task |
| Script Task | yes (8.2+) | FEEL or user-defined job type |
| Manual Task | yes | No automation |
| Business Rule Task | yes | DMN decision reference |
| Task (generic) | yes | No automation |
| Start Event | yes | None / Message / Signal / Timer |
| End Event | yes | None / Message / Signal / Terminate / Error |
| Intermediate Catch Event | yes | Message / Signal / Timer / Link |
| Intermediate Throw Event | yes | None / Message / Signal / Escalation / Link |
| Boundary Event | yes | Interrupting + non-interrupting; Message / Signal / Timer / Error / Escalation |
| Exclusive Gateway | yes | `default` attribute |
| Parallel Gateway | yes | — |
| Inclusive Gateway | yes (8.1+) | `default` attribute |
| Event-Based Gateway | yes | — |
| Complex Gateway | **no** | — |
| Sub-Process | yes | Embedded + Event Sub-Process |
| Event Sub-Process | yes | `triggeredByEvent="true"` |
| Call Activity | yes | Uses `zeebe:calledElement processId` extension, not BPMN `calledElement` attribute |
| Transaction | **no** | — |
| Escalation | yes (8.2+) | Throwing/catching on events; limited vs. C7 |

## Service Task Implementation — Job Workers Only

Zeebe has exactly one implementation kind:

| Kind | Extension element | Value shape |
|---|---|---|
| `JOB_WORKER` | `<zeebe:taskDefinition type="..." />` | Dot-separated job type, e.g. `newsletter.sendConfirmationMail` |

Example:

```xml
<bpmn:serviceTask id="serviceTask_SendEmail" name="Send email">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="newsletter.sendEmail" retries="3" />
  </bpmn:extensionElements>
</bpmn:serviceTask>
```

Source: `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/extractor/ZeebeImplementationKind.kt`

Style guide implications:
- Only the job-type schema rule applies. No DELEGATE_EXPRESSION, JAVA_DELEGATE, or EXPRESSION alternatives exist.
- The `type` attribute value can reference a process variable via `=variableName` (FEEL expression). Rule patterns should account for this.

## Async / Execution

Zeebe is asynchronous by default. There is **no `asyncBefore`, `asyncAfter`, or `exclusive` attribute**. Do not ask the user about async conventions for Zeebe.

## Variables

Variables are scoped per activity and mapped via:

- `<zeebe:ioMapping>` with `<zeebe:input source="..." target="..."/>` and `<zeebe:output source="..." target="..."/>`
- `<zeebe:loopCharacteristics>` for multi-instance, with `inputElement`, `inputCollection`, `outputElement`, `outputCollection`

No `camunda:properties` / `additionalVariables` mechanism.

## Messages

Zeebe messages **require a correlation key**:

```xml
<bpmn:message id="Message_OrderConfirmed" name="orderConfirmed">
  <bpmn:extensionElements>
    <zeebe:subscription correlationKey="=orderId" />
  </bpmn:extensionElements>
</bpmn:message>
```

The correlation key is a FEEL expression (note the `=` prefix). Style guide rules can enforce presence and naming of the correlation key expression.

## Signals

Global broadcast, no correlation. Same BPMN definition shape as C7.

## Errors

Error definitions use `name` + `errorCode`, like C7. Thrown via end events or by job workers.

## Escalations

Supported from Zeebe 8.2+. Fewer event-type combinations than C7. Check `shared/bpmn/c8-send-newsletter.bpmn` for a working example.

## Timers

Inside `<bpmn:timerEventDefinition>`, Zeebe expects FEEL expressions:

- `<bpmn:timeDate>=orderDate</bpmn:timeDate>` (FEEL expression → `=` prefix) or literal ISO-8601 (`2024-01-01T00:00:00Z`)
- `<bpmn:timeDuration>PT1H</bpmn:timeDuration>` (ISO-8601) or `=durationExpression`
- `<bpmn:timeCycle>R5/PT1H</bpmn:timeCycle>` or cron

Style guide implication: rule patterns on timer values need to tolerate both literal ISO-8601 and `=…` FEEL expressions.

## Call Activity

Uses a Zeebe extension, not the BPMN `calledElement` attribute:

```xml
<bpmn:callActivity id="callActivity_Payment">
  <bpmn:extensionElements>
    <zeebe:calledElement processId="payment-verification" />
  </bpmn:extensionElements>
</bpmn:callActivity>
```

## Example XML snippet

```xml
<bpmn:serviceTask id="serviceTask_SendEmail" name="Send email">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="newsletter.sendEmail" />
    <zeebe:ioMapping>
      <zeebe:input source="=recipient" target="to" />
    </zeebe:ioMapping>
  </bpmn:extensionElements>
</bpmn:serviceTask>

<bpmn:startEvent id="startEvent_OrderPlaced">
  <bpmn:messageEventDefinition messageRef="Message_OrderPlaced" />
</bpmn:startEvent>
```
