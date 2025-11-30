# ADR 010: Operaton Namespace-Only Extractor

## Status
Accepted

## Context
Operaton is a fork of Camunda 7 that uses its own namespace (`http://operaton.org/schema/1.0/bpmn`) for BPMN extension attributes. While Operaton can execute both Operaton-namespaced models and plain Camunda 7 models, we need to decide how to support Operaton in the extraction strategy.

### Options Considered

1. **Dual-namespace extractor**: Make `OperatonModelExtractor` support both `operaton:` and `camunda:` namespaces with fallback logic and more utils
2. **Namespace-only extractor**: Create a dedicated extractor that only handles `operaton:` namespace
3. **Extend Camunda extractor**: Modify `Camunda7ModelExtractor` to also check for `operaton:` namespace

## Decision
Implement `OperatonModelExtractor` to **only** support BPMN models with the Operaton namespace (`operaton:topic`, `operaton:delegateExpression`, `operaton:class`).

Users running Operaton with Camunda 7 models should use `ProcessEngine.CAMUNDA_7` configuration and the existing `Camunda7ModelExtractor`.

## Rationale

### Simplicity
- Avoids duplicate logic in a single extractor
- Each extractor has a clear, single responsibility
- Reduces code complexity and maintenance burden

### Separation of Concerns
- Follows the existing strategy pattern (see ADR 004)
- Operaton and Camunda 7 are separate engines with separate extractors
- Aligns with "one extractor per engine" principle

### User Flexibility
- Users can explicitly choose their extraction strategy via `ProcessEngine` enum
- Clear configuration: `OPERATON` for operaton-namespaced models, `CAMUNDA_7` for camunda-namespaced models
- No ambiguity about which namespace is being processed

## Consequences

### Positive
- **Clear responsibility**: Each extractor handles exactly one namespace
- **No duplication**: Avoids maintaining identical fallback logic in multiple extractors
- **Explicit configuration**: Users must consciously choose their engine type
- **Easier testing**: Each extractor has isolated, predictable behavior
- **Future-proof**: If Operaton diverges further from Camunda 7, no legacy compatibility code to remove

### Negative
- **Users must know their model namespace**: Those using Operaton engine with Camunda 7 models must configure `CAMUNDA_7`
- **Two extractors for similar engines**: Some code duplication between `OperatonModelExtractor` and `Camunda7ModelExtractor`

### Mitigation
- Document clearly in plugin configuration that `OPERATON` is for operaton-namespaced models only
- Provide clear error messages when models don't match expected namespace
- Users can easily switch between `CAMUNDA_7` and `OPERATON` via configuration

## Implementation
```kotlin
// ProcessEngine enum
enum class ProcessEngine {
    CAMUNDA_7,
    ZEEBE,
    OPERATON  // New engine option
}

// OperatonModelExtractor only checks operaton namespace
private fun ServiceTask.detectWorkerType(): String {
    return when {
        this.getAttributeValueNs(NAMESPACE, "topic") != null ->
            this.getAttributeValueNs(NAMESPACE, "topic")
        this.getAttributeValueNs(NAMESPACE, "delegateExpression") != null ->
            this.getAttributeValueNs(NAMESPACE, "delegateExpression")
        this.getAttributeValueNs(NAMESPACE, "class") != null ->
            this.getAttributeValueNs(NAMESPACE, "class")
        else -> throw IllegalStateException(
            "Service task '${this.id}' has no valid worker type"
        )
    }
}
```

## Alternatives Considered

### Alternative 1: Dual-Namespace Support
Make `OperatonModelExtractor` check both `operaton:` and `camunda:` namespaces with fallback logic.

**Rejected because:**
- Duplicates the entire `Camunda7ModelExtractor` logic within `OperatonModelExtractor`
- Violates single responsibility principle
- Makes testing more complex (need to test both namespace paths)
- Creates maintenance burden when either namespace changes

### Alternative 2: Extend Camunda Extractor
Modify `Camunda7ModelExtractor` to also recognize `operaton:` namespace.

**Rejected because:**
- Violates separation of concerns (one extractor handling two engines)
- Makes `CAMUNDA_7` configuration ambiguous (does it support Operaton?)
- Harder to diverge implementations if Operaton adds unique features
- Contradicts the strategy pattern established in ADR 004

## Related ADRs
- [ADR 004: Strategy Pattern for Multi-Engine](004-strategy-pattern-multi-engine.md) - Established pattern for engine-specific extractors
