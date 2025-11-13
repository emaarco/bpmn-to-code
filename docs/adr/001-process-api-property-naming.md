# ADR 001: Process API Property Naming Convention

## Status
Accepted

## Context
Generated constant properties derive their names from BPMN model element IDs (e.g., `Timer_EveryDay`, `subscriptionId`, `Activity-SendMail`). Using these identifiers directly as constant names violates Kotlin and Java naming conventions, causing IDE warnings and linter errors.

## Decision
Generated constant names follow language-specific naming conventions rather than preserving BPMN element ID casing:

- **Kotlin/Java constants**: Transform to `UPPER_SNAKE_CASE`
- **Constant values**: Preserve original BPMN identifiers unchanged

### Examples
```kotlin
// Before (mixed casing from BPMN)
const val Timer_EveryDay: String = "Timer_EveryDay"
const val subscriptionId: String = "subscriptionId"

// After (standardized naming)
const val TIMER_EVERY_DAY: String = "Timer_EveryDay"
const val SUBSCRIPTION_ID: String = "subscriptionId"
```

## Consequences

### Positive
- Eliminates IDE warnings and linter errors
- Improves code readability with consistent naming
- Follows official Kotlin and Java style guides
- Better number handling: `Timer_After3Days` � `TIMER_AFTER_3_DAYS`

### Negative

- BPMN element IDs no longer directly map to constant names

