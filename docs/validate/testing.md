# 🧪 Testing Module

::: warning Beta
This module is in beta. The API may change in a future release. [Leave feedback](https://github.com/emaarco/bpmn-to-code/issues) if you're using it.
:::

The `bpmn-to-code-testing` library lets you write architecture tests for your BPMN process models — the same way [ArchUnit](https://www.archunit.org/) lets you write architecture tests for Java code.

Add it to your test scope, write a test, and your CI will catch modeling issues before they reach production.

## Dependency

::: code-group

```kotlin [Gradle]
dependencies {
    testImplementation("io.github.emaarco:bpmn-to-code-testing:2.0.1")
}
```

```xml [Maven]
<dependency>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-testing</artifactId>
    <version>2.0.1</version>
    <scope>test</scope>
</dependency>
```

:::

## Basic Usage

```kotlin
import io.github.emaarco.bpmn.testing.BpmnValidator
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

@Test
fun `BPMN models should have no violations`() {
    BpmnValidator
        .fromClasspath("bpmn/")           // loads all .bpmn files from classpath:bpmn/
        .engine(ProcessEngine.ZEEBE)
        .validate()
        .assertNoViolations()
}
```

## Loading BPMN Files

| Method | What it does |
|--------|-------------|
| `BpmnValidator.fromClasspath("bpmn/")` | Loads all `.bpmn` files from the given classpath path |
| `BpmnValidator.fromDirectory(path)` | Loads all `.bpmn` files from a filesystem path (`java.nio.file.Path`) |

## Selecting Rules

By default, `validate()` runs all 11 built-in rules. You can override the rule set:

```kotlin
BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.CAMUNDA_7)
    .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
    .validate()
    .assertNoViolations()
```

Or pass a list:

```kotlin
.withRules(BpmnRules.all().filter { it.severity == Severity.ERROR })
```

## Disabling Rules

```kotlin
BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.ZEEBE)
    .disableRules("invalid-identifier")
    .validate()
    .assertNoViolations()
```

## Treating Warnings as Failures

```kotlin
BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.ZEEBE)
    .failOnWarning()
    .validate()
    .assertNoViolations()
```

## Assertions

`validate()` returns a `BpmnValidationAssert` with AssertJ-style assertions:

| Assertion | What it checks |
|-----------|---------------|
| `.assertNoViolations()` | No violations at all (neither errors nor warnings) |
| `.assertNoViolations("rule-id")` | No violations for the given rule |
| `.assertHasViolations()` | At least one violation |
| `.assertNoErrors()` | No ERROR-severity violations |
| `.assertNoWarnings()` | No WARN-severity violations |
| `.result()` | Returns the raw `ValidationResult` for custom assertions |

```kotlin
val result = BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.ZEEBE)
    .validate()

result.assertNoErrors()
result.assertNoViolations("invalid-identifier")  // custom: this rule is allowed to warn
```

## Built-in Rules Reference

| Rule | `BpmnRules` constant | Severity | Trigger |
|------|---------------------|----------|---------|
| Service task has no implementation | `MISSING_SERVICE_TASK_IMPLEMENTATION` | ERROR | Service task without jobType / delegate |
| Message event has no name | `MISSING_MESSAGE_NAME` | ERROR | Message start/catch/throw without a message name |
| Error event has no definition | `MISSING_ERROR_DEFINITION` | ERROR | Error boundary/end event without error definition |
| Signal event has no name | `MISSING_SIGNAL_NAME` | ERROR | Signal start/intermediate/end without signal name |
| Timer event has no definition | `MISSING_TIMER_DEFINITION` | ERROR | Timer event without type or value |
| Call activity has no calledElement | `MISSING_CALLED_ELEMENT` | ERROR | Call activity without `calledElement` attribute |
| Flow node has no ID | `MISSING_ELEMENT_ID` | ERROR | Any flow node missing an `id` attribute |
| Process has no ID | `MISSING_PROCESS_ID` | ERROR | Process element missing the `id` attribute |
| Process is empty | `EMPTY_PROCESS` | ERROR | Process with no flow nodes |
| Element ID produces invalid identifier | `INVALID_IDENTIFIER` | WARN | ID that cannot be converted to valid UPPER_SNAKE_CASE |
| Variable name collision | `COLLISION_DETECTION` | ERROR | Two different IDs normalize to the same constant name |

## Writing Custom Rules

Implement the `BpmnValidationRule` interface to add project-specific checks:

```kotlin
class RequireElementPrefixRule : BpmnValidationRule {

    override val id = "require-element-prefix"
    override val severity = Severity.WARN

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.flowNodes
            .filter { !it.id.contains("_") }
            .map { node ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = node.id,
                    processId = context.model.processId,
                    message = "Element '${node.id}' has no type prefix (e.g. 'Activity_', 'Task_').",
                )
            }
    }
}
```

Use it in tests:

```kotlin
BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.ZEEBE)
    .withRules(*BpmnRules.all().toTypedArray(), RequireElementPrefixRule())
    .validate()
    .assertNoViolations()
```

::: tip ValidationContext
`context.model` gives you the full `BpmnModel` — flow nodes, service tasks, messages, signals, errors, timers, and variables. `context.engine` tells you which engine was selected, so you can write engine-specific rules.
:::
