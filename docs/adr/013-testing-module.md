# ADR 013: Testing Module as a Separate Library

## Status
Accepted

## Context
bpmn-to-code generates code from BPMN models. Before generation, some validation was possible (e.g. collision detection throws at generation time). But there was no standalone way to validate a BPMN model against architectural conventions — outside of the generation pipeline.

Two related needs were identified:

1. **Build-time validation independent of code generation** — teams want to validate their BPMN models in CI without always regenerating code. A dedicated Gradle task (`validateBpmnModels`) or Maven goal (`validate-bpmn`) should be possible.

2. **Test-code validation** — teams want to write BPMN architecture tests in their test suite, the same way [ArchUnit](https://www.archunit.org/) allows writing Java architecture tests. This requires a testImplementation dependency, not a build plugin.

## Decision
Extract validation into a separate library: `bpmn-to-code-testing`.

**Why separate from core:**
- `bpmn-to-code-core` is a dependency of the build plugins. Adding AssertJ (a test library) to core would force it into compile scope for all users, even those who don't use the testing module.
- The testing module has a distinct dependency surface (AssertJ, JUnit 5) that belongs in `testImplementation` scope.
- Separation makes the boundary clear: `core` generates, `testing` validates.

**API design — fluent builder (like ArchUnit):**
```kotlin
BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.ZEEBE)
    .withRules(BpmnRules.all())
    .disableRules("invalid-identifier")
    .failOnWarning()
    .validate()
    .assertNoViolations()
```

The fluent builder was chosen over a static assertion API because it mirrors ArchUnit's style, which BPMN developers are likely familiar with, and naturally supports optional configuration (rules, disabled rules, warning policy).

**Built-in rules exposed via `BpmnRules`:**
All rules are exposed as `@JvmField` constants on `BpmnRules` for Java/Kotlin compatibility. `BpmnRules.all()` returns the full list.

**Extensibility:**
Users implement `BpmnValidationRule` to add custom rules. The interface requires only `id`, `severity`, and `validate(context)` — enough to express any model-level check.

## Consequences

### Positive
- Teams can write BPMN architecture tests alongside unit tests
- `bpmn-to-code-testing` does not add test libraries to production classpaths
- The same rule engine backs both the testing module and the build-time validation tasks
- Custom rules integrate seamlessly with built-in rules via `withRules(...)`

### Negative
- Extra dependency to add for teams that want test-level validation
- Two separate ways to run validation (build task vs test code) — teams need to decide which they use, or use both for different purposes
