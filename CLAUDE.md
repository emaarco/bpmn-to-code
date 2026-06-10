# Agent Instructions

This file provides guidance to AI coding agents when working with code in this repository.

## Project Overview

bpmn-to-code is a Gradle and Maven plugin that generates type-safe API definitions from BPMN process models. The project consists of:

- **bpmn-to-code-core**: Core logic for parsing BPMN files and generating API code (Kotlin)
- **bpmn-to-code-gradle**: Gradle plugin wrapper
- **bpmn-to-code-maven**: Maven plugin wrapper
- **bpmn-to-code-web**: Web plugin wrapper
- **bpmn-to-code-testing**: Arch-Unit like feature that allows us to test bpmnModels for specific rules

## Architecture

`bpmn-to-code-core` is a **Kotlin Multiplatform** module. The guiding rule:

> **`commonMain` is the pure, platform-agnostic generation engine** (BPMN domain model →
> code / JSON / validation, with no IO and no parsing). **Each platform brings its own parser,
> filesystem and entry point on top.** Because `commonMain` never calls platform code, there is
> **no `expect`/`actual`** in this module.

- **`commonMain`** — domain, the synchronous generation cores, the code/JSON emitter.
- **`jvmMain`** — the Gradle/Maven plugins' hexagon: the use-case services + their inbound/extract
  ports, Camunda parsing (Camunda 7 / Operaton / Zeebe), and `java.nio` filesystem.
- **`jsMain`** — a thin Node CLI: a bpmn-moddle parser, Node `fs` adapters, and the `cli` that calls
  the common cores directly. **Zeebe-only.**

Tests are in `src/jvmTest` / `src/jsTest`.

### Domain Layer (`commonMain/.../domain/`)
- `BpmnModel.kt`, `BpmnModelApi.kt`: Core domain entities
- `shared/`: Common types like `OutputLanguage`, `ProcessEngine`, `ServiceTaskDefinition`
- `service/ModelMergerService.kt`, `service/BpmnValidationService.kt`: merge + validation logic

### Application Layer
- `commonMain/.../application/`: the synchronous cores `ProcessApiGeneration` /
  `ProcessJsonGeneration` / `ProcessValidation` (validate → merge → generate, over already-parsed
  models) + the `GenerateApiCodePort` / `GenerateJsonPort` they use.
- `jvmMain/.../application/`: the port-driven use-case `service/`s + their `port/inbound/` and the
  synchronous `ExtractBpmnPort`. The JS CLI does not use these — it calls the common cores directly
  (bpmn-moddle parses asynchronously).

### Adapter Layer (`bpmn-to-code-core/src/{commonMain,jvmMain,jsMain}/kotlin/io/github/emaarco/bpmn/adapter/`)
- `outbound/codegen/`: multiplatform code emitter + Java/Kotlin builders (`commonMain`)
- `outbound/json/`: JSON descriptor generation (`commonMain`)
- `outbound/engine/`: BPMN parsing — Camunda model API (`jvmMain`), bpmn-moddle (`jsMain`)
- `outbound/filesystem/`: file IO — `java.nio` (`jvmMain`), Node `fs` (`jsMain`)
- `inbound/` (plugins, `jvmMain`) and `cli/` (npx CLI, `jsMain`): entry points

## Common Commands

### One-time setup
Install [Lefthook](https://github.com/evilmartians/lefthook) and register the git hooks (runs coverage check before push):
```bash
brew install lefthook  # or see docs/development/contributing.md for other platforms
lefthook install
```

### Build and Test
```bash
# Build entire project
./gradlew build

# Run tests for the core (KMP — runs JVM and JS)
./gradlew :bpmn-to-code-core:allTests

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


## Testing Strategy

Tests are organized by layer:
- Unit tests for domain services and builders
- Integration tests for adapters and extractors
- Test resources include sample BPMN files and expected API outputs

The project uses JUnit 5, AssertJ, and MockK for testing.

## Best Practices

### Test-Driven Development

Follow **TDD** when planning and implementing changes: update the domain model first (if applicable), then write/update tests to express the expected behavior (RED phase), then implement the production code to make them pass (GREEN phase).

### Verify After Each Task

After completing each discrete task (e.g., a phase in a plan, a refactor step, a bug fix), run a Gradle build on the affected modules to confirm compilation and tests still pass. Use targeted module builds (e.g., `./gradlew :bpmn-to-code-core:allTests`) rather than a full project build when only specific modules were changed.

### Always Consider Testing Impact

When making code changes, always think about the testing implications:

- **Write new tests** for new functionality or behavior changes
- **Update existing tests** when modifying expected outputs or behavior
- **Run affected tests** to verify changes work correctly before committing
- **Update test fixtures** (like expected output files) when generation logic changes

Example: When modifying code generators (e.g., `KotlinApiBuilder`), remember to:
1. Update the corresponding expected output files in `src/jvmTest/resources/`
2. Run the specific test suite to verify the changes
3. Check if other builders or tests are affected

### GitHub
- Use the `gh` CLI for GitHub operations.
- Keep commit messages and PR descriptions short. Focus on what changed and why.
- For issues: write a summary, current state, and desired state. Give a high-level overview of technical impact (breaking or not). Focus on behavior, not implementation details.

## Personality

You are a knowledgeable colleague, not someone who passively takes orders. If something proposed doesn't look right, suggest corrections, ask critical questions, and push back where needed. Challenge ideas that could benefit from further improvement or iterative refinement rather than just accepting them at face value.

## AI Skills

Reusable skill definitions live in `.claude/skills/`. New skills should be created under `.claude/skills/<skill-name>/SKILL.md`. See [docs/development/ai-skills.md](docs/development/ai-skills.md) for details.

