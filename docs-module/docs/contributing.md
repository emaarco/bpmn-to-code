# Contributing

Community contributions welcome! Here's how to get involved.

## Getting Started

### Prerequisites

- **JDK 21** or higher
- **Gradle 8+** (included via wrapper)
- Git

### Clone Repository

```bash
git clone https://github.com/emaarco/bpmn-to-code.git
cd bpmn-to-code
```

### Build Project

```bash
# Build entire project
./gradlew build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :bpmn-to-code-core:test
```

## Project Structure

```
bpmn-to-code/
├── bpmn-to-code-core/      # Core logic (Kotlin)
├── bpmn-to-code-gradle/    # Gradle plugin
├── bpmn-to-code-maven/     # Maven plugin
├── bpmn-to-code-web/       # Web application (Ktor)
├── examples/               # Example projects
└── docs/                   # Documentation
```

### Key Modules

**bpmn-to-code-core**: All business logic
- `domain/` - Business entities and logic
- `application/` - Use cases and ports
- `adapter/` - Technical implementations

**bpmn-to-code-gradle/maven**: Plugin wrappers (delegate to core)

**bpmn-to-code-web**: Ktor web server (delegates to core)

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

Follow existing patterns:

- Hexagonal architecture (domain → application → adapter)
- Strategy pattern for engines and languages
- Comprehensive tests for new features

### 3. Run Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :bpmn-to-code-core:test

# Specific test
./gradlew :bpmn-to-code-core:test --tests KotlinApiBuilderTest
```

### 4. Update Tests

When changing code generation:

1. Update expected output files in `src/test/resources/`
2. Run tests to verify
3. Check if other tests are affected

### 5. Commit Changes

```bash
git add .
git commit -m "feat: add support for new engine"
```

**Commit message format**:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation
- `refactor:` - Code refactoring
- `test:` - Test changes

### 6. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create pull request on GitHub.

## Testing Guidelines

### Unit Tests

Test domain logic in isolation:

```kotlin
@Test
fun `should merge BPMN models with same process ID`() {
    // Arrange
    val model1 = BpmnModel(/* ... */)
    val model2 = BpmnModel(/* ... */)

    // Act
    val merged = modelMerger.merge(listOf(model1, model2))

    // Assert
    assertThat(merged.elements).hasSize(10)
}
```

### Integration Tests

Test adapters with real BPMN files:

```kotlin
@Test
fun `should extract elements from Zeebe BPMN`() {
    val bpmn = loadBpmnFile("test-process.bpmn")
    val model = zeebeExtractor.extract(bpmn)
    assertThat(model.elements).contains("Activity_SendMail")
}
```

### Code Generation Tests

Verify generated code matches expected output:

```kotlin
@Test
fun `should generate Kotlin API with correct structure`() {
    val api = BpmnModelApi(/* ... */)
    val generated = kotlinBuilder.generate(api)
    val expected = loadResource("expected-output.kt")
    assertThat(generated).isEqualTo(expected)
}
```

## Building Documentation Locally

### Prerequisites

- Python 3.9+

### Setup

```bash
# Navigate to docs-module folder
cd docs-module

# Create Python virtual environment
python3 -m venv venv

# Activate
source venv/bin/activate

# Install dependencies (this will take a minute)
pip install --upgrade pip
pip install -r requirements.txt

# Start the server
mkdocs serve
```

### Build & Serve

```bash
# From docs-module/ directory
mkdocs serve

# Access at http://127.0.0.1:8000
```

**OR**

```bash
# Build static site
mkdocs build

# Output in docs-module/site/ directory
```

### Generate API Documentation

```bash
# From project root
./gradlew :bpmn-to-code-core:dokkaHtml
```

### Validate

```bash
# From docs-module/ directory
mkdocs build
```

> Note: Use `mkdocs build --strict` to catch all warnings as errors. Git revision warnings can be ignored for moved files.

## Common Tasks

### Add Support for New Engine

1. Implement `BpmnFileExtractorPort` in `adapter/outbound/engine/`
2. Add tests in `adapter/outbound/engine/`
3. Register in `ProcessEngine` enum
4. Update documentation

### Add Support for New Language

1. Implement `ApiCodeGeneratorPort` in `adapter/outbound/codegen/`
2. Add tests with expected output files
3. Register in `OutputLanguage` enum
4. Update documentation

### Add New BPMN Element Type

1. Update domain model (`BpmnModel`, `BpmnModelApi`)
2. Update extractors to parse new element
3. Update builders to generate code for new element
4. Add tests

## Code Style

- **Kotlin**: Follow Kotlin coding conventions
- **Formatting**: Use IntelliJ default formatter
- **Naming**: Descriptive names, avoid abbreviations
- **Comments**: Only where logic isn't self-evident

## Pull Request Guidelines

**Good PR**:
- Focused on single feature/fix
- Includes tests
- Updates documentation if needed
- Clear description of changes
- Compact commit messages

**PR Description Template**:
```markdown
## Summary
Brief description of changes

## Motivation
Why this change is needed

## Changes
- Change 1
- Change 2

## Testing
How it was tested
```

## Getting Help

- **Issues**: [GitHub Issues](https://github.com/emaarco/bpmn-to-code/issues)
- **Discussions**: [GitHub Discussions](https://github.com/emaarco/bpmn-to-code/discussions)
- **Architecture**: See [architecture documentation](architecture.md)

## Resources

- [Architecture Overview](architecture.md)
- [ADR Documentation](https://github.com/emaarco/bpmn-to-code/tree/main/docs/adr)
- [JUnit 5 Docs](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Docs](https://assertj.github.io/doc/)
- [Kotlin Conventions](https://kotlinlang.org/docs/coding-conventions.html)

---

*Thank you for contributing to bpmn-to-code!*
