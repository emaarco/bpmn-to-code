# ✅ Build-time Validation

::: warning Experimental
The `validateBpmnModels` Gradle task and `validate-bpmn` Maven goal are experimental. The API may change in a future release.
:::

bpmn-to-code can validate your BPMN models against a set of built-in rules — independently of code generation. Validation runs during your build and fails CI if your models violate any rules.

## What It Checks

| Rule ID | Severity | What it catches |
|---------|----------|-----------------|
| `missing-service-task-implementation` | ERROR | Service task with no implementation (no `jobType` / `delegateExpression` / class) |
| `missing-message-name` | ERROR | Message event or receive task with no message name |
| `missing-error-definition` | ERROR | Error boundary/end event with no error definition |
| `missing-signal-name` | ERROR | Signal event with no signal name |
| `missing-timer-definition` | ERROR | Timer event with no timer type or value |
| `missing-called-element` | ERROR | Call activity with no `calledElement` reference |
| `missing-element-id` | ERROR | Flow node with no ID |
| `missing-process-id` | ERROR | Process with no `id` attribute |
| `empty-process` | ERROR | Process with no flow nodes |
| `invalid-identifier` | WARN | Element ID that produces an invalid UPPER_SNAKE_CASE identifier in the generated API |
| `collision-detection` | ERROR | Two different element IDs that normalize to the same constant name (post-merge) |

## Gradle

### Configuration

```kotlin
tasks.named("validateBpmnModels", ValidateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    processEngine = ProcessEngine.ZEEBE
    failOnWarning = false                          // optional — treat warnings as errors
    disabledRules = setOf("invalid-identifier")   // optional — skip specific rules
}
```

### Running

```bash
./gradlew validateBpmnModels
```

The task is registered automatically by the plugin. It runs independently of `generateBpmnModelApi` — you can run validation without generating code.

### Example output

```
> Task :validateBpmnModels
[EXPERIMENTAL] The 'validateBpmnModels' task is experimental and may change in future releases.
[BPMN VALIDATION WARN]  newsletterSubscription/Activity_SendWelcomeMail: Service task has no implementation. Add a zeebe:taskDefinition with a type attribute. (rule: missing-service-task-implementation)
[BPMN VALIDATION ERROR] newsletterSubscription/Timer_EveryDay: Timer event has no timer definition. (rule: missing-timer-definition)

> Task :validateBpmnModels FAILED
BPMN validation failed: 1 error(s), 1 warning(s)
```

## Maven

### Configuration

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>2.0.0</version>
    <executions>
        <execution>
            <id>validate-bpmn</id>
            <goals><goal>validate-bpmn</goal></goals>
            <phase>verify</phase>
            <configuration>
                <baseDir>${project.basedir}</baseDir>
                <filePattern>src/main/resources/**/*.bpmn</filePattern>
                <processEngine>ZEEBE</processEngine>
                <failOnWarning>false</failOnWarning>
                <!-- optional: disable specific rules -->
                <disabledRules>
                    <disabledRule>invalid-identifier</disabledRule>
                </disabledRules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Running

```bash
mvn verify
# or directly:
mvn bpmn-to-code:validate-bpmn
```

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `baseDir` | `String` | required | Base directory for resolving relative paths |
| `filePattern` | `String` | required | Glob pattern to locate BPMN files |
| `processEngine` | `ProcessEngine` | required | `ZEEBE`, `CAMUNDA_7`, or `OPERATON` |
| `failOnWarning` | `Boolean` | `false` | Treat WARN-severity violations as build failures |
| `disabledRules` | `Set<String>` | `emptySet()` | Rule IDs to skip |

## Disabling Rules

Pass rule IDs to `disabledRules` to skip specific checks:

::: code-group

```kotlin [Gradle]
tasks.named("validateBpmnModels", ValidateBpmnModelsTask::class) {
    disabledRules = setOf("invalid-identifier", "empty-process")
}
```

```xml [Maven]
<disabledRules>
    <disabledRule>invalid-identifier</disabledRule>
    <disabledRule>empty-process</disabledRule>
</disabledRules>
```

:::

## Writing Custom Rules in Tests

The build-time validation covers the most common issues. For project-specific conventions, use the [Testing Module](/validate/testing) to write your own rules as part of your test suite.
