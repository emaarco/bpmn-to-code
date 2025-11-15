# Architecture Decision Records

## Project Purpose

**bpmn-to-code** is a tool that generates type-safe API definitions from BPMN process models. Available as Gradle/Maven plugins and a web application, its vision is to foster clean and robust solutions for BPMN-based process automation by:

- **Keeping models and code in sync**: Automatically extract process elements and generate code that references them
- **Reducing manual effort**: Eliminate tedious manual referencing of BPMN element IDs, messages, and task types
- **Enabling extensibility**: Support multiple process engines (Camunda 7, Zeebe) and output languages (Kotlin, Java)
- **Promoting clean design**: Future styleguide validation will ensure BPMN models follow naming conventions and patterns

The plugin transforms BPMN files into lightweight "Process API" code that integrates seamlessly with testing frameworks, messaging systems, and automation logic.

## Quality Attributes

The architecture prioritizes:

- **Extensibility**: Adding new engines or languages requires minimal changes to existing code
- **Testability**: Hexagonal architecture enables isolated testing of business logic
- **Maintainability**: Clear separation between domain logic and technical adapters
- **Standard compliance**: Generated code follows language-specific naming conventions

## Architecture Decision Records

Architecture Decision Records document significant architectural choices made in this project. Each ADR captures:

- **Context**: The situation requiring a decision
- **Decision**: What was decided and why
- **Consequences**: Trade-offs, benefits, and future implications
- **Alternatives**: Other options considered and why they were rejected

### When to Create an ADR

Document decisions that:
- Impact system structure or component interactions
- Affect extensibility, performance, or maintainability
- Introduce new patterns or architectural styles
- Have non-obvious trade-offs worth recording

### ADR Lifecycle

- **Proposed**: Decision under discussion
- **Accepted**: Decision approved and implemented
- **Deprecated**: Decision no longer recommended
- **Superseded**: Replaced by newer ADR

## Records

### Core Architecture
- [ADR 001: Hexagonal Architecture](001-hexagonal-architecture.md) - Clean architecture with ports and adapters
- [ADR 002: Model Merging](002-model-merging.md) - Combining multiple BPMN files into single API

### Code Generation
- [ADR 003: Generated API Structure](003-generated-api-structure.md) - Structure of generated Process APIs
- [ADR 005: Strategy Pattern for Code Generation](005-strategy-pattern-code-generation.md) - Language-specific builders (Java/Kotlin)

### Multi-Engine Support
- [ADR 004: Strategy Pattern for Multi-Engine](004-strategy-pattern-multi-engine.md) - Supporting Camunda 7 and Zeebe

### Features
- [ADR 006: File-Based Versioning](006-file-based-versioning.md) - API versioning strategy
- [ADR 007: Variable Extraction Scope](007-variable-extraction-scope.md) - Explicit variable definitions only

### Web Module
- [ADR 008: Web Module for Browser-Based Access](008-web-module-for-browser-access.md) - Strategic decision for web application
- [ADR 009: Ktor with Static Frontend (Single Module)](009-ktor-static-frontend-single-module.md) - Technology choices for web module
