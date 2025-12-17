# Architecture

bpmn-to-code uses hexagonal architecture (ports & adapters) for clean separation of concerns and extensibility.

## System Overview

```
┌─────────────────────────────────────────────────────────┐
│                    User-Facing                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐         │
│  │  Gradle  │  │  Maven   │  │     Web      │         │
│  │  Plugin  │  │  Plugin  │  │  Application │         │
│  └────┬─────┘  └────┬─────┘  └──────┬───────┘         │
│       └─────────────┼────────────────┘                 │
└─────────────────────┼──────────────────────────────────┘
                      │
        ┌─────────────▼────────────────┐
        │    bpmn-to-code-core         │
        │                              │
        │  ┌────────────────────────┐  │
        │  │  Domain Layer          │  │
        │  │  (Business Logic)      │  │
        │  └──────────┬─────────────┘  │
        │             │                │
        │  ┌──────────▼─────────────┐  │
        │  │  Application Layer     │  │
        │  │  (Use Cases & Ports)   │  │
        │  └──────────┬─────────────┘  │
        │             │                │
        │  ┌──────────▼─────────────┐  │
        │  │  Adapter Layer         │  │
        │  │  (Technical Details)   │  │
        │  │  • Code Generation     │  │
        │  │  • BPMN Parsing        │  │
        │  │  • File I/O            │  │
        │  └────────────────────────┘  │
        └───────────────────────────────┘
```

## Module Structure

### bpmn-to-code-core

**Purpose**: Core business logic for BPMN parsing and code generation

**Layers**:

- **Domain** (`domain/`): Pure business logic, no dependencies
  - `BpmnModel`, `BpmnFile`, `BpmnModelApi` - Core entities
  - `ModelMergerService` - Merges BPMN variants into unified API
  - Shared types: `OutputLanguage`, `ProcessEngine`, `ServiceTaskDefinition`

- **Application** (`application/`): Use cases and port definitions
  - `GenerateProcessApiUseCase` - Main use case interface
  - Inbound/outbound ports for dependency inversion

- **Adapter** (`adapter/`): Technical implementations
  - **Code Generation**: `KotlinApiBuilder`, `JavaApiBuilder`
  - **BPMN Parsing**: `Camunda7Extractor`, `ZeebeExtractor`, `OperatonExtractor`
  - **File System**: `BpmnFileLoader`
  - **Versioning**: `VersionService`

### bpmn-to-code-gradle

**Purpose**: Gradle plugin wrapper

**Key Classes**:
- `BpmnToCodeGradlePlugin` - Plugin entry point
- `GenerateBpmnModelsTask` - Gradle task

Delegates to `bpmn-to-code-core`.

### bpmn-to-code-maven

**Purpose**: Maven plugin wrapper

**Key Classes**:
- `BpmnModelMojo` - Maven mojo

Delegates to `bpmn-to-code-core`.

### bpmn-to-code-web

**Purpose**: Ktor-based web application

**Stack**:
- Ktor (HTTP server)
- Kotlinx Serialization (JSON)
- Static frontend (HTML/JS)

Delegates to `bpmn-to-code-core`.

## Key Design Decisions

### Hexagonal Architecture (ADR-001)

**Why**: Separation of business logic from technical details enables:

- **Testability**: Test domain logic without infrastructure
- **Extensibility**: Add new engines/languages without changing core
- **Maintainability**: Clear boundaries between layers

**Trade-offs**:
- More files and interfaces
- Worth it for long-term maintainability

### Strategy Pattern for Engines (ADR-004)

**Why**: Support multiple BPMN engines (Camunda 7, Zeebe, Operaton)

**How**: `BpmnFileExtractorPort` interface with engine-specific implementations

**Benefit**: Adding new engine = implement one interface

### Strategy Pattern for Languages (ADR-005)

**Why**: Generate both Kotlin and Java code

**How**: `ApiCodeGeneratorPort` interface with `KotlinApiBuilder` and `JavaApiBuilder`

**Benefit**: Add new language = implement one interface

### Model Merging (ADR-002)

**Why**: Support BPMN variants (dev/staging/prod) with same process ID

**How**: Merge all variants into single unified API containing superset of elements

**Use Case**: Same process, different configurations per environment

### Variable Extraction Scope (ADR-007)

**Why**: Only extract variables from explicit I/O mappings (not expressions)

**Rationale**:
- BPMN as single source of truth
- Clear API surface
- Reduced coupling

**Impact**: Variables in expressions (conditions, scripts) are not included in API

### Generated API Structure (ADR-003)

**Why**: Nested objects for organization

**Structure**:
```kotlin
object ProcessApi {
    const val PROCESS_ID: String
    object Elements { ... }
    object Messages { ... }
    object TaskTypes { ... }
    object Variables { ... }
}
```

**Benefits**:
- Clear organization
- IDE autocomplete
- Namespace separation

## Extension Points

Want to extend bpmn-to-code? Implement these interfaces:

### Add New Process Engine

Implement `BpmnFileExtractorPort`:

```kotlin
interface BpmnFileExtractorPort {
    fun extract(bpmnFile: BpmnFile): BpmnModel
}
```

Examples: `Camunda7Extractor`, `ZeebeExtractor`, `OperatonExtractor`

### Add New Output Language

Implement `ApiCodeGeneratorPort`:

```kotlin
interface ApiCodeGeneratorPort {
    fun generate(model: BpmnModelApi): String
}
```

Examples: `KotlinApiBuilder`, `JavaApiBuilder`

## Quality Attributes

**Extensibility**: New engines/languages via strategy pattern

**Testability**: Hexagonal architecture enables isolated unit testing

**Maintainability**: Clear layer separation, ports & adapters

**Standard Compliance**: Generated code follows language conventions

## Full ADR Documentation

For detailed architecture decisions, see:

- [ADR Index](https://github.com/emaarco/bpmn-to-code/tree/main/docs/adr)
- [ADR-001: Hexagonal Architecture](https://github.com/emaarco/bpmn-to-code/blob/main/docs/adr/001-hexagonal-architecture.md)
- [ADR-002: Model Merging](https://github.com/emaarco/bpmn-to-code/blob/main/docs/adr/002-model-merging.md)
- [ADR-003: Generated API Structure](https://github.com/emaarco/bpmn-to-code/blob/main/docs/adr/003-generated-api-structure.md)
- [ADR-004: Multi-Engine Strategy](https://github.com/emaarco/bpmn-to-code/blob/main/docs/adr/004-strategy-pattern-multi-engine.md)
- [ADR-005: Code Generation Strategy](https://github.com/emaarco/bpmn-to-code/blob/main/docs/adr/005-strategy-pattern-code-generation.md)

## Related

- [Contributing](contributing.md) - Development setup
- [Gradle](gradle.md) - Gradle plugin
- [Maven](maven.md) - Maven plugin
- [Web](web.md) - Web application
- [GitHub](https://github.com/emaarco/bpmn-to-code) - Source code
