# ADR 002: Model Merging for Environment Variants

## Status
Accepted

## Context
The same process may exist in different variants across environments (dev vs prod) or organizational units (location A vs location B). While these variants share the same `processId` and core structure, they differ in specific modeling details or execution behavior. Generating separate APIs for each variant would create substantial code duplication since they share the same kernel.

## Decision
Merge BPMN models with identical process IDs into a single unified model:

```kotlin
models.groupBy { it.processId }
  .map { (id, variants) -> mergeModelsWithSameId(id, variants) }
```

All elements from variants are combined using `distinctBy { it.getName() }` to create one comprehensive API containing the superset of all elements across variants.

## Consequences

### Positive
- **Eliminates duplication**: Single API for process regardless of variant count
- **Comprehensive coverage**: Generated API contains all possible elements across environments
- **Simpler integration**: One import instead of environment-specific APIs

### Negative
- **Element conflicts**: If variants define the same element ID differently, last-seen wins silently
- **API bloat**: Generated API may include elements not used in all environments
- **Non-deterministic**: File processing order determines conflict resolution

## Future Improvements
- Detect and warn about element conflicts across variants
- Add variant metadata to generated API documentation
- Support optional variant-specific sub-APIs

## Implementation
Merging occurs in `ModelMergerService` before code generation, ensuring single API per process ID.
