# ADR 004: Strategy Pattern for Multi-Engine Support

## Status
Accepted

## Context
Different BPMN process engines (Camunda 7, Zeebe) use different XML namespaces, element attributes, and extension mechanisms. Extracting process information requires engine-specific parsing logic.

## Decision
Use Strategy pattern with a registry of engine-specific extractors:

```kotlin
Map<ProcessEngine, EngineSpecificExtractor>
```

Each extractor implements `EngineSpecificExtractor` interface and handles its engine's specifics. The `ExtractBpmnAdapter` selects the appropriate extractor based on the target engine.

## Consequences

### Positive
- **Extensibility**: New engines added by implementing `EngineSpecificExtractor`
- **Separation**: Engine-specific logic isolated in dedicated classes
- **Maintainability**: Changes to one engine don't affect others
- **Clear contract**: Interface defines what extraction must provide

### Negative
- Cannot share parsing logic between similar engines (code duplication)
- Adding common functionality requires updating all extractors

## Implementation
```kotlin
// Interface
interface EngineSpecificExtractor {
    fun extract(file: File): BpmnModel
}

// Registry
val extractors = mapOf(
    ProcessEngine.ZEEBE to ZeebeModelExtractor(),
    ProcessEngine.CAMUNDA_7 to Camunda7ModelExtractor()
)
```

Future engines (e.g., Flowable, jBPM) can be added by creating new extractor implementations.
