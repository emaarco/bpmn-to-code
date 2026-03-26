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

bpmn-to-code only extracts variables from I/O mappings — not from expressions in sequence flows, gateways, or script tasks.

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

## Additional Variables (Camunda 7 / Operaton)

For elements where I/O mappings aren't supported (e.g. message start events in Camunda 7), use extension properties:

```xml
<camunda:properties>
  <camunda:property name="additionalVariables" value="orderId, customerEmail, amount" />
</camunda:properties>
```

See [Camunda 7 engine page](/engines/camunda7#additional-variables-extension-properties) for details.

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

