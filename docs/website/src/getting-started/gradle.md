# 🚀 Gradle Setup

The bpmn-to-code Gradle plugin generates type-safe Process API files from your BPMN models as part of your Gradle build. It's available on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle) and takes just a few minutes to set up.

## 1. Apply the plugin

::: code-group

```kotlin [build.gradle.kts]
plugins {
    id("io.github.emaarco.bpmn-to-code-gradle") version "1.0.0"
}
```

```groovy [build.gradle]
plugins {
    id 'io.github.emaarco.bpmn-to-code-gradle' version '1.0.0'
}
```

:::

Make sure the Gradle Plugin Portal is in your `settings.gradle.kts`:

::: code-group

```kotlin [settings.gradle.kts]
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

```groovy [settings.gradle]
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

:::

## 2. Configure the generation task

::: code-group

```kotlin [build.gradle.kts]
import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = false
}
```

```groovy [build.gradle]
import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = false
}
```

:::

See [Configuration](/guide/configuration) for all available parameters.

## 3. Generate the API

```bash
./gradlew generateBpmnModelApi
```

The generated Process API file(s) will appear in your configured output folder.

## Automated setup with AI Skills

Using [Claude Code](https://docs.anthropic.com/en/docs/claude-code)? The `setup-bpmn-to-code-gradle` skill can configure the plugin for you automatically — it detects your project structure, finds your BPMN files, and adds the right configuration.

After setup, use the `migrate-to-bpmn-to-code-apis` skill to replace hardcoded BPMN strings across your codebase with references to the generated Process API.

```bash
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-bpmn-to-code-gradle
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/migrate-to-bpmn-to-code-apis
```

See [AI Skills](/skills/) for all available skills.

## Advanced configuration

Need multiple engines, separate packages per domain, or file filtering? See [Gradle Advanced Configuration](/getting-started/gradle-advanced).
