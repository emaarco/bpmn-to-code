# Best Practices for Process Modeling with bpmn-to-code

This guide provides best practices for creating BPMN process models that work optimally with `bpmn-to-code`, while also promoting clean and maintainable process automation in general.

## General BPMN Best Practices

### Model Clarity
- **Name everything explicitly**: Every element (tasks, events, gateways) should have a descriptive name that explains its purpose
- **Keep processes focused**: Each process should represent a single business capability or workflow
- **Use meaningful IDs**: Element IDs should reflect the element's purpose, not generic names like "Task_1" or "Event_2"
- **Document complex logic**: Use annotations or external documentation for non-obvious business rules

### Maintainability
- **BPMN as a single source of truth**: Your BPMN model should be complete and understandable without inspecting implementation code
- **Minimize technical details**: Focus on business logic in the model; keep technical implementation details in your code
- **Version control your BPMN files**: Track changes to process models alongside your code

## Naming Conventions

When using bpmn-to-code, your element IDs and names directly shape the generated API. Consistent, descriptive naming improves code readability and makes the generated Process API easier to use. Consider organizing your BPMN elements with clear prefixes to create well-structured APIs.

### Recommended Naming Patterns

**Use consistent prefixes** to improve generated API organization and readability:
- Tasks: `Activity_SendEmail`, `Activity_ProcessPayment`
- Events: `StartEvent_FormSubmitted`, `EndEvent_OrderCompleted`
- Timers: `Timer_After3Days`, `Timer_EveryMorning`
- Messages: `Message_OrderReceived`, `Message_PaymentConfirmed`
- Errors: `Error_InvalidData`, `Error_PaymentFailed`
- Signals: `Signal_CancellationRequested`

**Avoid:**
- Generic IDs: `Task_1`, `Event_abc123`
- Special characters beyond underscores
- IDs that don't reflect the element's purpose

**Benefits:**
- Clean, readable generated constants
- Better IDE autocomplete experience
- Easier to understand the generated API
- Compliance with Java/Kotlin coding standards

## Variable Management

### Always Define Variables Explicitly

**Do:** Use explicit I/O mappings on tasks and events
```xml
<!-- Camunda 7 -->
<camunda:inputOutput>
  <camunda:inputParameter name="subscriptionId">${subscriptionId}</camunda:inputParameter>
  <camunda:outputParameter name="mailSent">true</camunda:outputParameter>
</camunda:inputOutput>

<!-- Zeebe -->
<zeebe:ioMapping>
  <zeebe:input source="=subscriptionId" target="subscriptionId" />
  <zeebe:output source="=mailSent" target="mailSent" />
</zeebe:ioMapping>
```

**Don't rely on:** 
- Variables only referenced in expressions. They will not be included in the generated API.
```xml
<!-- This variable won't appear in generated API -->
<bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
  ${execution.getVariable('decision') == 'ACCEPTED'}
</bpmn:conditionExpression>
```

### Why Explicit Definitions Matter

bpmn-to-code extracts variables from **all explicit variable definition mechanisms** in the BPMN model — not from expressions in sequence flows, gateways, or script tasks.

Supported sources by engine:

| Source | Camunda 7 / Operaton | Zeebe |
|--------|----------------------|-------|
| I/O mappings (`inputOutput`) | ✅ | ✅ |
| Multi-instance attributes | ✅ | ✅ |
| Call activity in/out mappings | ✅ | — |
| `additionalVariables` extension properties | ✅ | — |

See the engine pages for details: [Camunda 7](/engines/camunda7) · [Operaton](/engines/operaton) · [Zeebe](/engines/zeebe)

**Rationale:**
- **BPMN as single source of truth**: All process variables should be visible in the model itself
- **Clear API surface**: The generated API only includes variables the model explicitly declares
- **Reduced coupling**: Process API doesn't leak implementation details from worker code
- **Better maintainability**: All variable contracts are visible in the BPMN model

**Impact:** If a variable isn't explicitly defined through one of the above mechanisms, it won't appear in your generated Process API's `Variables` object.

### When I/O Mappings Aren't Available

Some elements don't support I/O mappings — for example, **message start events** in Camunda 7 and Operaton. For these cases, use `additionalVariables` extension properties to declare variables explicitly:

```xml
<!-- Camunda 7 -->
<bpmn:extensionElements>
  <camunda:properties>
    <camunda:property name="additionalVariables" value="orderId, customerEmail, amount" />
  </camunda:properties>
</bpmn:extensionElements>

<!-- Operaton -->
<bpmn:extensionElements>
  <operaton:properties>
    <operaton:property name="additionalVariables" value="orderId, customerEmail, amount" />
  </operaton:properties>
</bpmn:extensionElements>
```

This works on any BPMN element. See [Camunda 7](/engines/camunda7#additional-variables-extension-properties) and [Operaton](/engines/operaton#additional-variables-extension-properties) engine pages for full details.

## Multi-Environment Modeling

### Process Variants Across Environments

bpmn-to-code automatically **merges BPMN models with identical process IDs** into a single unified API.

**Use case:** The same process exists in different variants (dev vs prod, location A vs location B) but shares the same `processId`.

**How it works:**
```
dev-order-process.bpmn    → processId="orderProcess"
prod-order-process.bpmn   → processId="orderProcess"
                          ↓
OrderProcessApi (merged elements from both variants)
```

### Best Practices for Variants

**Do:**
- Use the same `processId` for all variants of the same process
- Keep core process structure consistent across variants
- Use variants for environment-specific configurations (different timers, different service endpoints)

**Be aware:**
- Generated API contains the **superset** of all elements across variants
- API may include elements not used in all environments
- If variants define the same element ID differently, the behavior is non-deterministic (last-seen wins)

**Avoid:**
- Defining the same element ID with different semantics across variants
- Creating substantially different process flows with the same `processId`

### Alternative Approach

If process variants are significantly different, consider:
- Using **different process IDs** for each variant
- Generating separate APIs for each variant
- Using version suffixes in process IDs: `orderProcess_v1`, `orderProcess_v2`

## Summary

**Key Takeaways:**
1. Name elements descriptively and consistently using prefix patterns
2. Define all process variables explicitly using I/O mappings, call activity mappings, or `additionalVariables` where needed
3. Keep process variants aligned when using the same process ID
5. Let your BPMN model be the single source of truth

Following these practices ensures clean generated APIs, maintainable process models, and seamless integration with bpmn-to-code.
