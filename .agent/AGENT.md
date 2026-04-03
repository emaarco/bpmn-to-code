# Agent Instructions

This file provides guidance to AI coding agents when working with code in this repository.

## Project Overview

bpmn-to-code is a Gradle and Maven plugin that generates type-safe API definitions from BPMN process models. The project consists of:

- **bpmn-to-code-core**: Core logic for parsing BPMN files and generating API code (Kotlin)
- **bpmn-to-code-gradle**: Gradle plugin wrapper
- **bpmn-to-code-maven**: Maven plugin wrapper

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

### Verify After Each Task

After completing each discrete task (e.g., a phase in a plan, a refactor step, a bug fix), run a Gradle build on the affected modules to confirm compilation and tests still pass. Use targeted module builds (e.g., `./gradlew :bpmn-to-code-core:test`) rather than a full project build when only specific modules were changed.

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

### GitHub
- Use the `gh` CLI for GitHub operations.
- Keep commit messages and PR descriptions short. Focus on what changed and why.
- For issues: write a summary, current state, and desired state. Give a high-level overview of technical impact (breaking or not). Focus on behavior, not implementation details.

## AI Skills

Reusable skill definitions live in `.agent/skills/`. Agent-specific symlinks (e.g., `.claude` → `.agent`) provide compatibility. New skills should be created under `.agent/skills/<skill-name>/SKILL.md`. See [docs/development/ai-skills.md](docs/development/ai-skills.md) for details.

