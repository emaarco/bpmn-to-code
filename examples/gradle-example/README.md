# bpmn-to-code Gradle Plugin Example

This module is demonstrating the usage of the **bpmn-to-code Gradle Plugin**.

## ✨ How to Use

To get started, apply the plugin in your build.gradle.kts file:

```kotlin
plugins {
    id("io.github.emaarco.bpmn-to-code-gradle") version "{{latestVersion}}"
}
```

If the plugin can not be found yet, please make sure,
that you've also added the gradle plugin repository to your settings.gradle.kts file:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

Next, configure the generation task by setting its parameters.
This configuration directs the plugin on where to locate your BPMN files,
where to generate the API code, and which settings to apply (language, package, and process engine).
Once configured, bpmn-to-code-gradle processes your BPMN models
and creates convenient, type-safe references for your application.

```kotlin
import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "de.emaarco.example"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```
