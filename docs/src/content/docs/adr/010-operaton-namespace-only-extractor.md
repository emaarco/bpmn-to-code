---
title: "ADR 010: Operaton Namespace-Only Extractor"
description: Dedicated extractor for Operaton engine that only handles the operaton namespace.
---

## Status
Accepted

## Context
Operaton is a fork of Camunda 7 that uses its own namespace (`http://operaton.org/schema/1.0/bpmn`) for BPMN extension attributes. While Operaton can execute both Operaton-namespaced models and plain Camunda 7 models, we need to decide how to support Operaton in the extraction strategy.

### Options Considered

1. **Dual-namespace extractor**: Make `OperatonModelExtractor` support both `operaton:` and `camunda:` namespaces with fallback logic
2. **Namespace-only extractor**: Create a dedicated extractor that only handles `operaton:` namespace
3. **Extend Camunda extractor**: Modify `Camunda7ModelExtractor` to also check for `operaton:` namespace

## Decision
Implement `OperatonModelExtractor` to **only** support BPMN models with the Operaton namespace (`operaton:topic`, `operaton:delegateExpression`, `operaton:class`).

Users running Operaton with Camunda 7 models should use `ProcessEngine.CAMUNDA_7` configuration and the existing `Camunda7ModelExtractor`.

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

## Implementation
```kotlin
// ProcessEngine enum
enum class ProcessEngine {
    CAMUNDA_7,
    ZEEBE,
    OPERATON
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

## Related ADRs
- [ADR 004: Strategy Pattern for Multi-Engine](004-strategy-pattern-multi-engine) — Established pattern for engine-specific extractors
