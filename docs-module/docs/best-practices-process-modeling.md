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

bpmn-to-code **only extracts variables from I/O mappings**, not from expressions in sequence flows, gateways, or script tasks.

**Rationale:**
- **BPMN as single source of truth**: All process variables should be visible in the model itself
- **Clear API surface**: The generated API only includes variables the model explicitly declares
- **Reduced coupling**: Process API doesn't leak implementation details from worker code
- **Better maintainability**: All variable contracts are visible in the BPMN model

**Impact:** If a variable isn't explicitly defined in I/O mappings, it won't appear in your generated Process API's `Variables` object.

## API Versioning

The plugin provides a `useVersioning` configuration option that generates versioned API classes (e.g., `ProcessApiV1`, `ProcessApiV2`).

### Why useVersioning Is Often Not Worth It

When versioning is enabled, every time you increase the version, you must migrate **all imports** to the new version. If you don't, your codebase will spread imports across multiple versions over time, creating maintenance burden and confusion.

**The migration effort typically outweighs the benefits.**

### Recommended Approach: Fail Fast

Instead of using versioning, keep `useVersioning=false` (the default) and let breaking changes fail at compile time:

1. Make breaking changes to your BPMN model (rename/delete elements)
2. Regenerate the Process API
3. Compilation errors immediately show you where code needs updating
4. Fix the errors and update your code
5. Put deprecated elements in a separate file if needed for backwards compatibility

**Benefits:**
- Immediate feedback on what needs to be updated
- No version management overhead
- Single source of truth (always using latest API)
- Forces you to keep code in sync with models

**Drawbacks:**
- Breaking changes require manual intervention

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
2. Define all process variables explicitly in I/O mappings
3. Avoid API versioning unless absolutely necessary; prefer fail-fast compilation errors
4. Keep process variants aligned when using the same process ID
5. Let your BPMN model be the single source of truth

Following these practices ensures clean generated APIs, maintainable process models, and seamless integration with bpmn-to-code.
