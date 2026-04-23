# 📖 Common Patterns

Practical patterns and best practices for working with bpmn-to-code.

## Naming Conventions

Your element IDs directly shape the generated API. Use consistent prefixes for clean, readable constants:

| Element Type | Pattern | Example |
|-------------|---------|---------|
| Tasks | `Activity_` | `Activity_SendEmail`, `Activity_ProcessPayment` |
| Start Events | `StartEvent_` | `StartEvent_FormSubmitted` |
| End Events | `EndEvent_` | `EndEvent_OrderCompleted` |
| Timers | `Timer_` | `Timer_After3Days`, `Timer_EveryMorning` |
| Messages | `Message_` | `Message_OrderReceived` |
| Errors | `Error_` | `Error_InvalidData` |
| Signals | `Signal_` | `Signal_CancellationRequested` |

**Avoid:** generic IDs like `Task_1` or `Event_abc123`.

## Explicit Variable Definitions

bpmn-to-code only extracts variables from **explicit variable definitions** in the BPMN model — not from expressions in sequence flows, gateways, or script tasks. The supported sources depend on the engine:

| Source | Camunda 7 / Operaton | Zeebe |
|--------|----------------------|-------|
| I/O mappings | ✅ | ✅ |
| Multi-instance attributes | ✅ | ✅ |
| Call activity in/out mappings | ✅ | — |
| `additionalInputVariables` / `additionalOutputVariables` extension properties | ✅ | — |

**Do:** Define variables explicitly in I/O mappings.

::: code-group

```xml [Camunda 7]
<camunda:inputOutput>
  <camunda:inputParameter name="subscriptionId">${subscriptionId}</camunda:inputParameter>
  <camunda:outputParameter name="mailSent">true</camunda:outputParameter>
</camunda:inputOutput>
```

```xml [Zeebe]
<zeebe:ioMapping>
  <zeebe:input source="=subscriptionId" target="subscriptionId" />
  <zeebe:output source="=mailSent" target="mailSent" />
</zeebe:ioMapping>
```

:::

**Don't rely on** variables only referenced in expressions:

```xml
<!-- This variable won't appear in the generated API -->
<bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
  ${execution.getVariable('decision') == 'ACCEPTED'}
</bpmn:conditionExpression>
```

## Additional Input / Output Variables (Camunda 7 / Operaton)

Some elements don't support I/O mappings — for example, **message start events** in Camunda 7 and Operaton. Variables arriving with the triggering message won't be captured by `camunda:inputOutput` / `operaton:inputOutput`, so they would be missing from the generated API.

The workaround is to declare them explicitly using two directional extension properties — `additionalInputVariables` and `additionalOutputVariables`. Values are routed to the element's `Inputs` / `Outputs` sub-object accordingly:

::: code-group

```xml [Camunda 7]
<bpmn:extensionElements>
  <camunda:properties>
    <camunda:property name="additionalInputVariables" value="orderId, customerEmail" />
    <camunda:property name="additionalOutputVariables" value="processingResult" />
  </camunda:properties>
</bpmn:extensionElements>
```

```xml [Operaton]
<bpmn:extensionElements>
  <operaton:properties>
    <operaton:property name="additionalInputVariables" value="orderId, customerEmail" />
    <operaton:property name="additionalOutputVariables" value="processingResult" />
  </operaton:properties>
</bpmn:extensionElements>
```

:::

Each comma-separated value becomes a variable in the generated API under the corresponding direction. Works on any BPMN element, not just start events. The legacy undirected `additionalVariables` property is no longer extracted.

::: tip
See the engine pages for full details: [Camunda 7](/engines/camunda7#additional-input-output-variables-extension-properties) · [Operaton](/engines/operaton#additional-input-output-variables-extension-properties)
:::

## Multi-Environment Modeling

bpmn-to-code automatically merges BPMN models with identical process IDs into a single API.

```
dev-order-process.bpmn    -> processId="orderProcess"
prod-order-process.bpmn   -> processId="orderProcess"
                           |
                           v
OrderProcessApi (merged elements from both)
```

**Guidelines:**
- Use the same `processId` for all variants of the same process
- Keep core process structure consistent across variants
- The generated API contains the **superset** of all elements across variants

::: warning
If variants define the same element ID with different semantics, behavior is non-deterministic (last-seen wins). Use different process IDs for substantially different process flows.
:::

