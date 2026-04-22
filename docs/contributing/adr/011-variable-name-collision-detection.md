# ADR 011: Variable Name Collision Detection

## Status
Accepted

## Context
BPMN element IDs are converted to UPPER_SNAKE_CASE constants in generated APIs using `StringUtils.toUpperSnakeCase()`. This normalization replaces separators (hyphens, dots) with underscores and standardizes casing, which can cause silent data loss when different element IDs normalize to the same constant name.

**Examples of collisions:**
- `endEvent_dataProcessed` and `endEvent-dataProcessed` both → `END_EVENT_DATA_PROCESSED`
- `eventData` and `event-data` both → `EVENT_DATA`

Previously, when collisions occurred, only the first value was used during code generation, and subsequent values were silently ignored, leading to incorrect API mappings.

## Decision
Implement comprehensive collision detection that:

1. **Detects all collisions** across all variable mapping types (flow nodes, messages, signals, errors, timers, service tasks, variables) before throwing a single exception with complete details
2. **Distinguishes collisions from duplicates**:
   - **Collision**: Different source IDs normalize to same constant name → Error
   - **Duplicate**: Same source ID across models → Allowed (expected merging behavior)
3. **Provides actionable error messages** grouping collisions by process and showing all conflicting IDs
4. **Halts generation** to force users to fix BPMN modeling issues at the source

### Implementation

- Created `CollisionDetectionService` as a dedicated domain service following hexagonal architecture to detect collisions and throw errors
  
## Consequences

### Positive
- Users see all collision issues in one error message (single fix iteration)
- Clear distinction between true duplicates (valid) and collisions (invalid)
- Prevents silent data loss from undetected ID normalization conflicts
- Structured error messages with process context and conflicting ID lists
- Non-breaking for valid BPMN models (only detects actual problems)

### Negative
- Breaking change for BPMN models with existing (previously silent) collisions
- Users with collision issues must fix BPMN models before generation succeeds
- Additional validation overhead during model merging (minimal performance impact)

## Example Error Message
```
Variable name collisions detected in 2 processes:

Process: NewsletterSubscription
  [FlowNode] END_EVENT_DATA_PROCESSED
    Conflicting IDs: endEvent-dataProcessed, endEvent_dataProcessed
  [Message] MESSAGE_FORM_SUBMITTED
    Conflicting IDs: message-formSubmitted, message_formSubmitted

Process: UserRegistration
  [Signal] SIGNAL_REGISTRATION_COMPLETE
    Conflicting IDs: signal.registrationComplete, signalRegistrationComplete

Please update your BPMN files to use consistent naming.
```
