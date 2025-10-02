# 🚀 bpmn-to-code-gradle

bpmn-to-code is a plugin designed to simplify process automation.
Its vision is to foster clean & robust solutions for BPMN-based process automation.
Therefore, it aims to provide a range of features —
such as generating API definition files from BPMN process models —
to reduce manual effort, simplify testing,
promote the creation of clean process models,
and ensure consistency between your BPMN model and your code.

## ✨ How to Use

To get started, apply the plugin in your build.gradle.kts file:

```kotlin
plugins {
    id("io.github.emaarco.bpmn-to-code-gradle") version "0.0.8"
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
    useVersioning = true
}
```
