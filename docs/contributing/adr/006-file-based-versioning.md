# ADR 006: File-Based API Versioning

## Status
Deprecated — feature removed

## Context
BPMN processes evolve over time. When process structure changes, existing code using the old API may break. A versioning mechanism was introduced to support multiple API versions simultaneously during migration periods.

When enabled (`useVersioning = true`), the plugin tracked versions in a `bpmn-to-code.properties` file and appended a version suffix to generated class names (e.g., `NewsletterSubscriptionProcessApiV1`).

## Original implementation

The feature was wired through the full hexagonal stack:

- **Plugin layer**: Both `GenerateBpmnModelsTask` (Gradle) and `BpmnModelMojo` (Maven) exposed a `useVersioning` boolean parameter (default `false`).
- **Application layer**: `GenerateProcessApiService` branched on `useVersioning` in the command. When enabled, it delegated to `ApiVersioningPort` to read the current version, increment it, and pass it into `BpmnModelApi`.
- **Domain**: `BpmnModelApi.apiVersion: Int?` carried the version number. `fileName()` appended `V<n>` to the class name when non-null (e.g., `OrderProcessApiV3`).
- **Adapter layer**: `VersionService` implemented `ApiVersioningPort` by reading and writing a `bpmn-to-code.properties` file in the project's base directory, storing one `processId=version` entry per line.

The version was incremented on every generation run when versioning was enabled, and the updated value was written back to the properties file as a build side effect.

## Decision
The versioning feature has been removed from bpmn-to-code.

### Why it was removed

1. **No single source of truth**: With versioning enabled, multiple API files in different versions could coexist in the codebase. This caused usage to spread across versions — some code referencing `ProcessApiV1`, other code referencing `ProcessApiV2` — making it unclear which version was current and fragmenting the API surface.

2. **Accidental complexity**: The branching in the service layer to support versioned vs. unversioned generation complicated the core generation logic without proportional benefit.

3. **Build side effects**: The `VersionService` wrote to `bpmn-to-code.properties` during builds, causing unexpected dirty working trees in CI/CD pipelines.

4. **Virtually unused**: The feature defaulted to `false`, and real-world adoption was negligible.

### Recommended alternative

When breaking changes to a BPMN process require backward compatibility (e.g., in-flight process instances on the old model), use manually defined constants:

```kotlin
object OrderProcessApi {
    const val PROCESS_ID: String = "orderProcess"
    // ... current generated constants
}

@Deprecated("Use OrderProcessApi instead — old process version, retained for in-flight instances")
object OrderProcessApiLegacy {
    const val PROCESS_ID: String = "orderProcess"
    const val ACTIVITY_OLD_STEP: String = "Activity_OldStep" // removed in current model
}
```

This approach keeps a single generated API as the source of truth while giving explicit, `@Deprecated`-annotated fallbacks for elements that no longer exist in the current model. The deprecation warnings surface remaining usages during compilation, guiding migration without the overhead of automated versioning.

## Consequences

### Positive
- Simplified service layer with a single, linear generation path
- No build side effects (no file writes beyond generated code)
- Clearer API surface — one generated file per process, always current
- Reduced public API surface in both Gradle and Maven plugins

### Negative
- Users who had `useVersioning = true` lose the feature (breaking change)
- Manual effort required for backward-compatible constants when needed
