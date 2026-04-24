# ADR 014: Shared BPMN Types in Standalone Files

## Status
Superseded by [ADR 016](./016-runtime-artifact-for-shared-types.md) — shared types now live in the published `bpmn-to-code-runtime` artifact instead of a per-project `{packagePath}/types/` folder.

## Context
In 1.x, data classes used in the generated Process API (`BpmnTimer`, `BpmnError`) were emitted as nested classes inside the generated file:

```kotlin
object NewsletterSubscriptionProcessApi {
    object Timers {
        val TIMER_EVERY_DAY: BpmnTimer = BpmnTimer("Duration", "PT1M")

        data class BpmnTimer(val type: String, val timerValue: String)  // nested
    }
}
```

Two new sections introduced in 2.0 — `Flows` and `Relations` — require `BpmnFlow` and `BpmnRelations` types. `BpmnEscalation` was also added alongside `BpmnError`.

Keeping these as nested classes raised two problems:

1. **Type identity across files** — if a project has multiple BPMN processes, each generated file would define its own `BpmnTimer`. Code working with multiple processes would need to import from a specific process API, or hit type-mismatch errors when passing a `BpmnTimer` from one process API to a function expecting one from another.

2. **Duplication** — the `BpmnFlow` data class definition (with four fields) would be duplicated in every generated file, inflating the output and complicating IDE navigation.

## Decision
Extract `BpmnTimer`, `BpmnError`, `BpmnEscalation`, `BpmnFlow`, and `BpmnRelations` into standalone files in a `types/` subfolder under the configured `packagePath`:

```
{packagePath}/
  types/
    BpmnTimer.kt
    BpmnError.kt
    BpmnEscalation.kt
    BpmnFlow.kt
    BpmnRelations.kt
  NewsletterSubscriptionProcessApi.kt
  OrderProcessApi.kt
```

Each generated process API file imports from `{packagePath}.types`.

This is a **breaking change** for any code that referenced the nested types:

```kotlin
// 1.x — nested
val timer: NewsletterSubscriptionProcessApi.Timers.BpmnTimer = ...

// 2.0 — standalone
import com.example.process.types.BpmnTimer
val timer: BpmnTimer = ...
```

The values and field names are unchanged; only the import path changes.

## Consequences

### Positive
- `BpmnTimer`, `BpmnFlow`, etc. are shared types across all generated process APIs in a project — no type-mismatch errors when working with multiple processes
- The type definitions are generated once per project, not once per process
- Cleaner IDE navigation — types appear as top-level classes, not nested inside a specific process API

### Negative
- **Breaking change** — code referencing the old nested type paths must update imports
- Two output locations to manage: the `types/` folder and the process API files
