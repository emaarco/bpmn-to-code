# Gradle Plugin

Generate Process APIs from BPMN files in your Gradle builds.

## Installation

**Step 1**: Add plugin to `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.emaarco.bpmn-to-code-gradle") version "0.0.17"
}
```

**Step 2**: Ensure Gradle Plugin Portal is in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

## Configuration

Configure the generation task:

```kotlin
import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = false
}
```

### Configuration Parameters

| Parameter | Type | Description | Required | Default |
|-----------|------|-------------|----------|---------|
| `baseDir` | String | Base directory for file pattern resolution | Yes | - |
| `filePattern` | String | Ant-style pattern to match BPMN files | Yes | - |
| `outputFolderPath` | String | Directory where generated code will be written | Yes | - |
| `packagePath` | String | Package name for generated classes | Yes | - |
| `outputLanguage` | Enum | `OutputLanguage.KOTLIN` or `OutputLanguage.JAVA` | Yes | - |
| `processEngine` | Enum | `ProcessEngine.CAMUNDA_7`, `ZEEBE`, or `OPERATON` | Yes | - |
| `useVersioning` | Boolean | Enable file-based API versioning | No | `false` |

### File Pattern Examples

```kotlin
// All BPMN files in resources
filePattern = "src/main/resources/**/*.bpmn"

// Specific directory
filePattern = "src/main/resources/processes/*.bpmn"

// Exclude directories (dev, test)
filePattern = "src/main/resources/processes/!(dev|test)/**/*.bpmn"

// Only files ending with -process.bpmn
filePattern = "src/main/resources/**/*-process.bpmn"
```

## Usage

**Generate API:**

```bash
./gradlew generateBpmnModelApi
```

**Integrate with build:**

```kotlin
tasks.named("compileKotlin") {
    dependsOn("generateBpmnModelApi")
}
```

## Examples

### Basic Zeebe + Kotlin

```kotlin
tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

### Camunda 7 + Java

```kotlin
tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/java"
    packagePath = "com.example.processes"
    outputLanguage = OutputLanguage.JAVA
    processEngine = ProcessEngine.CAMUNDA_7
}
```

### Multi-Environment

```kotlin
// Production: exclude dev and test folders
tasks.register("generateProdApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/processes/!(dev|test)/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes.prod"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}

// Development: include all
tasks.register("generateDevApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/processes/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes.dev"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

## Troubleshooting

### Plugin not found

Ensure `gradlePluginPortal()` is in your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

### No BPMN files found

Check:

- `baseDir` is correct (usually `projectDir.toString()`)
- `filePattern` matches your file locations
- BPMN files have `.bpmn` extension

### Generated code has errors

Verify:

- `packagePath` doesn't conflict with existing packages
- BPMN element IDs are valid identifiers (alphanumeric + underscore)
- `outputLanguage` matches your project (Kotlin vs Java)

### Task types not generated

Task types are only extracted for **Zeebe** and **Operaton** engines. Camunda 7 processes won't have a `TaskTypes` object.

## Related

- [Maven Plugin](maven.md) - Maven alternative
- [Web Application](web.md) - No build tool required
- [Architecture](architecture.md) - How it works
- [GitHub](https://github.com/emaarco/bpmn-to-code) - Source code and issues
