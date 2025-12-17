# ADR 003: Generated API Structure and Naming

## Status
Accepted

## Context
Generated API must organize hundreds of constants (element IDs, messages, variables, etc.) from BPMN models. Needs a structure that prevents naming conflicts, provides IDE support, follows language conventions, and handles different property types (strings vs. structured data).

## Decision

### Nested Object Organization
Generate categorized nested structure:

```kotlin
object ProcessApi {
    const val PROCESS_ID: String = "..."

    object Elements { /* flow nodes */ }
    object Messages { /* message definitions */ }
    object TaskTypes { /* service task types */ }
    object Timers { /* timer definitions */ }
    object Errors { /* error definitions */ }
    object Signals { /* signal definitions */ }
    object Variables { /* process variables */ }
}
```

### UPPER_SNAKE_CASE Naming Convention
Transform BPMN identifiers to follow Kotlin/Java constant conventions:

```kotlin
// BPMN: "Timer_EveryDay" → Generated constant name
const val TIMER_EVERY_DAY: String = "Timer_EveryDay"

// BPMN: "subscriptionId" → Generated constant name
const val SUBSCRIPTION_ID: String = "subscriptionId"

// BPMN: "Timer_After3Days" → Generated constant name
const val TIMER_AFTER_3_DAYS: String = "Timer_After3Days"
```

Constant values preserve original BPMN identifiers unchanged. Conversion adds underscores at case boundaries and between letters/digits, then uppercases.

**Service Tasks & Messages**: For consistency, constant names are derived from the semantic identifier (for instance worker type for service tasks, message name for messages) rather than technical element IDs. This makes constants more meaningful and aligned with their business purpose. Moreover it allows us to generate a distinct list, since workerTypes and messageNames may be reused in multiple elements of a process:

```kotlin
// Service task with camunda:topic="newsletter.sendWelcomeMail"
const val NEWSLETTER_SEND_WELCOME_MAIL: String = "newsletter.sendWelcomeMail"

// Message with name="Message_FormSubmitted"
const val MESSAGE_FORM_SUBMITTED: String = "Message_FormSubmitted"
```

### Const vs Val for Property Types
Use `const val` for string constants, `val` for data class instances:

```kotlin
// String constants - compile-time
const val ACTIVITY_SEND_MAIL: String = "Activity_SendMail"

// Structured data - runtime
val TIMER_EVERY_DAY: BpmnTimer = BpmnTimer("Duration", "PT1M")
data class BpmnTimer(val type: String, val timerValue: String)
```

## Consequences

### Positive
- **Namespace isolation**: `Elements.TIMER` vs `Messages.TIMER` - no conflicts
- **IDE autocomplete**: Typing `Elements.` shows only relevant constants
- **Standards compliance**: Eliminates IDE warnings, follows style guides
- **Improved readability**: `TIMER_AFTER_3_DAYS` clearer than `Timer_After3Days`
- **Compile-time optimization**: String constants inlined at call sites
- **Type safety**: Data classes provide structured access for complex properties

### Negative
- **Verbosity**: Longer qualified names (`ProcessApi.Elements.TIMER`)
- **Breaking change**: UPPER_SNAKE_CASE transformation incompatible with previous mixed casing
- **Inconsistency**: Mixed property types (`const` vs `val`) due to Kotlin limitations
- **Migration burden**: Users must update all constant references when upgrading

## Alternatives Considered

**Flat structure with prefixes** (Rejected)
- Loses IDE categorization benefits
- All constants in single autocomplete list

**Preserve BPMN casing** (Rejected)
- Violates language conventions
- Causes IDE warnings and linter errors

**All `val` properties** (Rejected)
- Loses compile-time constant benefits for strings

## Implementation
- Conversion: `StringUtils.toUpperSnakeCase()` handles case transformation
- Structure: Builders (Kotlin/Java) create nested static classes/objects
- Properties: Const for strings, val for data classes (Kotlin compiler requirement)
