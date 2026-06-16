# ADR 016: Migration to the `io.miragon` Namespace

## Status
Accepted (supersedes the coordinates/package naming in [ADR 014](014-shared-bpmn-types.md))

## Context
bpmn-to-code shipped under the personal namespace `io.github.emaarco` — both the Maven group and the Kotlin/Java package root (`io.github.emaarco.bpmn.*`). To host and support the project under the [miragon](https://miragon.io) company brand, it moves to `io.miragon`:

- Maven group `io.github.emaarco` → `io.miragon`
- Gradle plugin id `io.github.emaarco.bpmn-to-code-gradle` → `io.miragon.bpmn-to-code-gradle`
- Package root `io.github.emaarco.bpmn.*` → `io.miragon.bpmn.*`

This is a breaking change for consumers on two surfaces: the coordinates in their build files, and the `io.github.emaarco.bpmn.runtime.*` imports baked into their **generated** Process API code. The decision is how to move without forcing every consumer to migrate in lock-step on upgrade.

## Decision
Ship the rename as **3.0.0** with a backward-compatibility layer that keeps the old namespace working but deprecated. The old surface is removed in 4.0.

Per-surface mechanism:

1. **Runtime types — duplicated `@Deprecated` classes in the new jar.** The new `io.miragon:bpmn-to-code-runtime` jar ships the canonical types at `io.miragon.bpmn.runtime.*` **and** `@Deprecated` copies at `io.github.emaarco.bpmn.runtime.*`. The copies must live in the *new* jar (not a separate bridge module) because the Gradle plugin auto-adds the runtime dependency and now adds the new coordinate — a consumer who bumps the plugin but hasn't regenerated only has the new jar on the classpath, so the old types must travel with it.

   They are **real duplicated classes**, not a Kotlin `typealias`: a `typealias` is invisible to Java, and the types are final `data class`/`enum` (so subclassing is out). Real classes are the only mechanism that keeps both Java and Kotlin generated code compiling. The old and new types are therefore distinct JVM types — consumers regenerate and switch imports together per module.

2. **Old coordinates — relocation POMs in a dedicated module.** `io.github.emaarco:bpmn-to-code-{runtime,maven,testing}` are published as POM-only artifacts carrying `<distributionManagement><relocation>` to `io.miragon:*`. Maven and Gradle both follow relocation, so unchanged build files keep resolving, and a published `…:3.0.0` relocation POM also makes the new version *discoverable* to anyone still on the old coordinate (Dependabot, `versions:display-dependency-updates`, …). This is the only correct way to keep a Maven `<plugin>` coordinate working.

   These three POMs live in a separate `bpmn-to-code-relocation` module rather than alongside the real artifacts, because `io.miragon` is an **org-owned** Central namespace while `io.github.emaarco` is **personally owned** — a single Central upload uses one credential, so the two namespaces must be published in separate runs. The release pipeline publishes the real `io.miragon` artifacts with the org token (`MIRAGON_SONATYPE_*`) and the relocation module with the personal token (`SONATYPE_*`), both signed with the same key.

3. **Old Gradle plugin id — deprecated wrapper plugin.** `io.github.emaarco.bpmn-to-code-gradle` stays registered as a thin `DeprecatedBpmnModelGeneratorPlugin` that applies the new plugin and logs a deprecation warning at configuration time.

## Consequences

### Positive
- Consumers upgrade to 3.0.0 without breakage and migrate to `io.miragon` at their own pace.
- Build-time deprecation warnings (Maven relocation message, Gradle wrapper warning, `@Deprecated` types with `ReplaceWith`) guide the migration.
- Generated code emits the new imports by default; the project is fully on `io.miragon` internally.

### Negative
- The compatibility surface (11 duplicated runtime types, 3 relocation publications, 1 wrapper plugin) must be maintained until 4.0.
- Gradle does not warn when following a relocation POM, so a consumer pinning the old dependency coordinate directly is nudged only by the `@Deprecated` type warnings. Documented in the [v3 Migration Guide](../../changelog/v3.md).
- The repository, SCM URLs, Docker image, and developer metadata stay under `emaarco` for now; a later move to a `miragon` GitHub organization is out of scope for this change.

## Alternatives
- **Hard cut (no compat layer).** Simpler, but breaks every consumer's build and generated code at once. Rejected — the project has published releases in the wild.
- **Legacy bridge modules instead of relocation + in-jar duplicates.** A separate `io.github.emaarco:bpmn-to-code-runtime` jar holding the old types and depending on the new one. Rejected for the runtime: the plugin auto-adds the *new* coordinate, so the bridge jar wouldn't be on the classpath for plugin users who haven't regenerated.
