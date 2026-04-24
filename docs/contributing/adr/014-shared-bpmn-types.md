# ADR 014: Shared BPMN Types via `bpmn-to-code-runtime` Artifact

## Status
Accepted

## Context
v1 emitted BPMN data types (`BpmnTimer`, `BpmnError`) as nested classes inside each generated Process API file:

```kotlin
object NewsletterSubscriptionProcessApi {
    object Timers {
        val TIMER_EVERY_DAY: BpmnTimer = BpmnTimer("Duration", "PT1M")

        data class BpmnTimer(val type: String, val timerValue: String)  // nested
    }
}
```

v2 adds more shared types — `BpmnEscalation`, `BpmnFlow`, `BpmnRelations`, plus the typed identifier wrappers `ProcessId`, `ElementId`, `MessageName`, `SignalName`, and the sealed `VariableName` with `Input`/`Output`/`InOut` subtypes. Keeping these nested or re-generating them per module hits two problems:

1. **Type identity across modules.** A common library in a multi-module project wants to expose wrappers like `fun startProcess(id: ProcessId)`. If every service module generates its own `ProcessId`, the common module has nothing to reference. Forcing every service into one `packagePath` creates split-package / duplicate-class conflicts and defeats module ownership.

2. **Duplication.** A 5+ field `BpmnRelations` data class re-generated in every consuming file bloats output and clutters IDE navigation.

## Decision
Extract the shared types into a **published artifact**, `io.github.emaarco:bpmn-to-code-runtime`. The generator emits Process API files that `import io.github.emaarco.bpmn.runtime.*`; the runtime types are hand-written Kotlin in a dedicated module.

Artifact contents:
- Identifier wrappers: `ProcessId`, `ElementId`, `MessageName`, `SignalName`
- Variable wrapper: sealed interface `VariableName` + nested `Input` / `Output` / `InOut`
- Metadata records: `BpmnTimer`, `BpmnError`, `BpmnEscalation`, `BpmnFlow`, `BpmnRelations`
- Engine enum: `BpmnEngine`

The Gradle plugin automatically adds `implementation("io.github.emaarco:bpmn-to-code-runtime:$pluginVersion")` to any project it's applied to (when the `java` plugin is present), matching the pattern of `org.jetbrains.kotlin.jvm` adding `kotlin-stdlib`. Maven users add the `<dependency>` manually — documented in the Maven plugin README.

### Data class, not value class

The runtime publishes identifier wrappers as Kotlin `data class`, not `@JvmInline value class`. Value classes are JVM-name-mangled on Kotlin method parameters (`fun startProcess(id: ProcessId)` compiles to `startProcess-<hash>(String)`), and `-` is not a legal Java identifier character. That makes Kotlin methods with value-class parameters unreachable from Java, which breaks the multi-module story whenever common or a service is Java-authored. A `data class` compiles to a regular JVM class and preserves type safety across both languages. The per-call boxing cost is one `String` wrapper — negligible against I/O-bound engine calls.

### Web playground

The web module bundles the runtime's Kotlin sources as classpath resources and serves them back in `GenerateResponse.libraryFiles`, so users can preview the shared types alongside their generated Process API. The ZIP download is lean by default (ProcessApi files + a `README.md` with the dep snippet), with an opt-in checkbox to include the runtime sources for a self-contained bundle that compiles without the dep. No forked generator behaviour — the web generator and the plugin emit identical output.

## Consequences

### Positive
- One `ProcessId` class on the classpath across all consuming modules. Common libraries can write typed wrappers that compile everywhere.
- Kotlin and Java interop both work fully. The runtime supports mixed codebases.
- The generator simplifies: shared-type builders are gone; Process API builders reference constant `ClassName`s instead of deriving them from `packagePath`.
- The runtime source is hand-written and unit-testable like any library.

### Negative
- Users must have the runtime on their classpath. Gradle handles this automatically; Maven users add one `<dependency>`.
- Identifier wrappers are no longer `@JvmInline value class` — a one-allocation cost per instance. Not observable in practice.
- The shared-type API surface is now a published contract; evolving it means semver discipline on the runtime module.
