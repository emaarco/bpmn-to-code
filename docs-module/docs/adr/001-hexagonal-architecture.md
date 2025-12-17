# ADR 002: Hexagonal Architecture (Ports & Adapters)

## Status
Accepted

## Context
The plugin must parse BPMN files from multiple engines (Camunda 7, Zeebe), generate code in multiple languages (Kotlin, Java), and integrate with different build systems (Gradle, Maven). A tightly coupled architecture would make adding new engines or languages difficult and reduce testability.

## Decision
Implement hexagonal architecture with clear separation:

- **Domain Layer**: Core business entities (`BpmnModel`, `BpmnFile`, `BpmnModelApi`) and services (`ModelMergerService`)
- **Application Layer**: Use cases (`GenerateProcessApiUseCase`) and port interfaces defining contracts
- **Adapter Layer**:
  - Inbound: Plugin entry points (`CreateProcessApiPlugin`)
  - Outbound: Engine extractors, code builders, file system access, versioning

Ports define interfaces; adapters provide implementations. Domain depends on nothing; adapters depend on ports.

## Consequences

### Positive
- Domain logic isolated from technical concerns
- Easy to add new engines/languages via new adapters
- Testable: mock ports for unit tests, real adapters for integration tests
- Clear dependency direction (inward toward domain)

### Negative
- More abstraction layers than simple layered architecture
- Additional interfaces increase initial complexity
- Requires discipline to maintain boundaries

## Implementation
```
bpmn-to-code-core/
├── domain/           # Entities, services (no dependencies)
├── application/      # Use cases, port interfaces
└── adapter/
    ├── inbound/      # Plugin entry points
    └── outbound/     # Engine, codegen, filesystem adapters
```
