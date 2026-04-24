# ADR 016: Runtime Artifact for Shared BPMN Types

## Status
Accepted. Supersedes [ADR 014](./014-shared-bpmn-types.md).

## Context

ADR 014 moved the shared BPMN types (`BpmnTimer`, `BpmnError`, `BpmnEscalation`, `BpmnFlow`, `BpmnRelations`) out of the per-process API file and into a `{packagePath}/types/` sub-folder generated once per plugin invocation. That addressed type identity and duplication within a single module.

It did not address the multi-module case. A typical layout has a `common` module exposing runtime-service wrappers (`fun startProcess(id: ProcessId)`) plus multiple service modules each with their own BPMN files and their own generated Process API. Because each service module generates its own `{packagePath}.types.ProcessId`, the common module has no shared `ProcessId` class to reference. Forcing every service into one `packagePath` creates split-package / duplicate-class conflicts and defeats module ownership.

The set of shared identifier wrappers introduced in v2 — `ProcessId`, `ElementId`, `MessageName`, `SignalName`, and the sealed `VariableName` with `Input`/`Output`/`InOut` subtypes — made the problem unavoidable: writing typed wrappers in a common module stops compiling the moment more than one service module is involved.

## Decision

Extract the shared types into a published artifact, `io.github.emaarco:bpmn-to-code-runtime`. The generator emits Process API files that `import io.github.emaarco.bpmn.runtime.*`; the runtime types themselves are hand-written Kotlin in the new module.

Artifact contents:
- Identifier wrappers: `ProcessId`, `ElementId`, `MessageName`, `SignalName`
- Variable wrapper: sealed interface `VariableName` + `Input`/`Output`/`InOut`
- Metadata records: `BpmnTimer`, `BpmnError`, `BpmnEscalation`, `BpmnFlow`, `BpmnRelations`
- Engine enum: `BpmnEngine`

The Gradle plugin automatically adds `implementation("io.github.emaarco:bpmn-to-code-runtime:$pluginVersion")` to any project it's applied to (when the `java` plugin is present), matching the pattern of `org.jetbrains.kotlin.jvm` adding `kotlin-stdlib`. Maven users add the `<dependency>` manually.

### Data class, not value class

The runtime publishes identifier wrappers as Kotlin `data class`, not `@JvmInline value class`. Value classes are JVM-name-mangled on Kotlin method parameters (`fun startProcess(id: ProcessId)` compiles to `startProcess-<hash>(String)`), and `-` is not a legal Java identifier character. That makes Kotlin methods with value-class parameters unreachable from Java, which breaks the multi-module story whenever common or a service is Java-authored. Data class compiles to a regular JVM class and preserves type safety across both languages.

The per-call boxing cost is one `String` wrapper — negligible against I/O-bound engine calls.

## Consequences

### Positive
- One `ProcessId` class on the classpath across all consuming modules. Common libraries can write typed wrappers that compile everywhere.
- Kotlin and Java interop both work fully. The runtime supports mixed codebases.
- The generator simplifies: the two `*SharedTypesBuilder`s are gone; Process API builders reference constant `ClassName`s instead of deriving them from `packagePath`.
- The runtime source is unit-testable like any library.

### Negative
- Users must have the runtime on their classpath. Gradle handles this automatically; Maven users add one `<dependency>`.
- Identifier wrappers are no longer `@JvmInline value class` — a one-allocation cost per instance. Not observable in practice.
- The shared-type API surface is now a published contract; evolving it means semver discipline on the runtime module.

### Web playground
The web module bundles the runtime's Kotlin sources as classpath resources and serves them back in `GenerateResponse.libraryFiles`, so users can preview the shared types alongside their generated Process API. The ZIP download is lean by default (ProcessApi files + a `README.md` with the dep snippet), with an opt-in checkbox to include the runtime sources for a self-contained bundle that compiles without the dep. No forked generator behaviour — the web generator and the plugin emit identical output.
