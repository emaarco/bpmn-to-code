# 🚀 bpmn-to-code-gradle

bpmn-to-code-gradle enables you to generate API-definition files from BPMN process models
within your Gradle projects. Its purpose is to extract vital process information
(like element names, message names, and service task types)
and convert them into Java or Kotlin API representations —
streamlining your process automation and reducing manual errors.

## 🎯 Purpose & Use-Case

This plugin is designed to automate the extraction of BPMN model data, so you can:

- Easily interact with processes via generated API definitions.
- Minimize manual errors by keeping your BPMN model and code in sync.
- Accelerate process testing, message handling, and integration efforts.

## ✨ How to Use

Add the plugin to your Gradle project by applying it in your `build.gradle.kts` file:

```kotlin
plugins {
    id("io.github.emaarco.bpmn-to-code-gradle") version "0.0.1"
}
```

Then, configure the generation task by setting up its parameters. For example, if you have a BPMN process managing
newsletter subscriptions, add the following configuration:

```
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

This configuration instructs the plugin where to find your BPMN files,
where to generate the API code, and how to generate it.

## 🤝 Contributing

Contributions are welcome! Please check the root repository for contribution guidelines.

## 📜 License

This project is licensed under the MIT License. See the LICENSE file for details.