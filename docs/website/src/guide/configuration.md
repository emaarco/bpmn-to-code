# ⚙️ Configuration

All plugin parameters, available for both the Gradle and Maven plugins.

## Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `baseDir` | `String` | yes | — | Base directory for resolving relative paths |
| `filePattern` | `String` | yes | — | Glob pattern to locate BPMN files (e.g. `src/main/resources/**/*.bpmn`) |
| `outputFolderPath` | `String` | yes | — | Directory where generated code is written |
| `packagePath` | `String` | yes | — | Package name for generated classes (e.g. `com.example.process`) |
| `outputLanguage` | `OutputLanguage` | yes | — | `KOTLIN`, `JAVA`, `TYPESCRIPT` ⚠️, or `GO` ⚠️ |
| `processEngine` | `ProcessEngine` | yes | — | `ZEEBE`, `CAMUNDA_7`, or `OPERATON` |

## Process Engines

| Engine | Value | Description |
|--------|-------|-------------|
| Camunda 8 / Zeebe | `ZEEBE` | Uses `zeebe:` namespace extensions |
| Camunda 7 | `CAMUNDA_7` | Uses `camunda:` namespace extensions |
| Operaton | `OPERATON` | Uses `operaton:` namespace (Operaton's own XML namespace) |

::: tip Operaton
Operaton is an open-source fork of Camunda 7. It uses the same patterns for I/O mappings and call activities, but with its own XML namespace (`http://operaton.org/schema/1.0/bpmn`). If your Operaton models still use `camunda:` namespace attributes, use `CAMUNDA_7` instead.
:::

## Output Languages

| Language | Value | Generated Output | Status |
|----------|-------|-----------------|--------|
| Kotlin | `KOTLIN` | `object` with `const val` properties | stable |
| Java | `JAVA` | `class` with `public static final` fields | stable |
| TypeScript | `TYPESCRIPT` | `export const` object with typed properties | experimental ⚠️ |
| Go | `GO` | package-level `var` structs | experimental ⚠️ |

::: warning Experimental languages
TypeScript and Go support is functional but not yet considered stable. Output format may change in future releases.
:::

## Examples

::: code-group

```kotlin [Gradle (Kotlin DSL)]
tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

```xml [Maven]
<configuration>
    <baseDir>${project.basedir}</baseDir>
    <filePattern>src/main/resources/*.bpmn</filePattern>
    <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
    <packagePath>com.example.process</packagePath>
    <outputLanguage>KOTLIN</outputLanguage>
    <processEngine>ZEEBE</processEngine>
</configuration>
```

:::
