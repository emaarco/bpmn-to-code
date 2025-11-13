# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

bpmn-to-code is a Gradle and Maven plugin that generates type-safe API definitions from BPMN process models. The project consists of:

- **bpmn-to-code-core**: Core logic for parsing BPMN files and generating API code (Kotlin)
- **bpmn-to-code-gradle**: Gradle plugin wrapper
- **bpmn-to-code-maven**: Maven plugin wrapper  
- **examples/**: Example projects demonstrating plugin usage

## Architecture

The core follows hexagonal architecture with clear separation of concerns:

### Domain Layer (`bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/domain/`)
- `BpmnModel.kt`, `BpmnFile.kt`, `BpmnModelApi.kt`: Core domain entities
- `shared/`: Common types like `OutputLanguage`, `ProcessEngine`, `ServiceTaskDefinition`
- `service/ModelMergerService.kt`: Business logic for merging BPMN models

### Application Layer (`bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/application/`)
- `port/inbound/GenerateProcessApiUseCase.kt`: Main use case interface
- `port/outbound/`: Adapter interfaces for external dependencies
- `service/GenerateProcessApiService.kt`: Use case implementation

### Adapter Layer (`bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/`)
- `inbound/CreateProcessApiPlugin.kt`: Entry point for plugins
- `outbound/codegen/`: Code generation adapters with Java/Kotlin builders
- `outbound/engine/`: BPMN parsing adapters for Camunda 7 and Zeebe
- `outbound/filesystem/BpmnFileLoader.kt`: File system operations
- `outbound/versioning/VersionService.kt`: API versioning logic

## Common Commands

### Build and Test
```bash
# Build entire project
./gradlew build

# Run tests for specific module
./gradlew :bpmn-to-code-core:test

# Run all tests
./gradlew test
```

### Code Generation Testing
```bash
# Test Gradle plugin
./gradlew :bpmn-to-code-gradle:test

# Test Maven plugin
./gradlew :bpmn-to-code-maven:test
```

### Plugin Development
The plugins generate code from BPMN files. Key configuration parameters:
- `filePattern`: BPMN file location pattern
- `outputFolderPath`: Where to generate code
- `packagePath`: Generated code package
- `outputLanguage`: KOTLIN or JAVA
- `processEngine`: CAMUNDA_7 or ZEEBE
- `useVersioning`: Enable API versioning

## Testing Strategy

Tests are organized by layer:
- Unit tests for domain services and builders
- Integration tests for adapters and extractors
- Test resources include sample BPMN files and expected API outputs

The project uses JUnit 5, AssertJ, and MockK for testing.

## Best Practices

### Always Consider Testing Impact

When making code changes, always think about the testing implications:

- **Write new tests** for new functionality or behavior changes
- **Update existing tests** when modifying expected outputs or behavior
- **Run affected tests** to verify changes work correctly before committing
- **Update test fixtures** (like expected output files) when generation logic changes

Example: When modifying code generators (e.g., `KotlinApiBuilder`), remember to:
1. Update the corresponding expected output files in `src/test/resources/`
2. Run the specific test suite to verify the changes
3. Check if other builders or tests are affected

### Productivity Tips: 
- When working with GitHub, you can use the gh cli tool

### GitHub
- Keep commit messages and body short and descriptive. Focus on what changed and why, not on technical details and adding bloat like author notes and co.
- When writing issues, pull requests or other stuff, also write compact.
