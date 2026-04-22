# Gradle Advanced Configuration

bpmn-to-code itself doesn't support excluding specific files or running multiple engines in a single task. But Gradle's build system covers both cases.

## Multiple tasks

When your project uses multiple engines, organizes BPMN files in separate directories, or needs different output packages — register a separate generation task for each group:

```kotlin
// Camunda 7 processes
tasks.register<GenerateBpmnModelsTask>("generateC7Api") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/c7/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.c7"
    processEngine = ProcessEngine.CAMUNDA_7
    outputLanguage = OutputLanguage.KOTLIN
}

// Zeebe processes
tasks.register<GenerateBpmnModelsTask>("generateZeebeApi") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/c8/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.c8"
    processEngine = ProcessEngine.ZEEBE
    outputLanguage = OutputLanguage.KOTLIN
}

// Run all at once
tasks.register("generateAll") {
    dependsOn("generateC7Api", "generateZeebeApi")
}
```

Common reasons to split into multiple tasks:
- Different engines (e.g. some processes on Zeebe, others on Camunda 7)
- Different output packages per domain or team
- Different output languages (e.g. Kotlin for new code, Java for legacy modules)

## Pre-filtering files

bpmn-to-code processes all files matching the `filePattern` glob — it has no built-in include/exclude logic. If you need to exclude specific files or cherry-pick a subset, use a Gradle [`Copy`](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Copy.html) task to collect the files you want into a staging directory, then point the generation task at that directory:

```kotlin
// Collect only the BPMN files you want
tasks.register<Copy>("collectBpmnFiles") {
    from("src/main/resources") {
        include("**/order-*.bpmn", "**/payment-*.bpmn")
        exclude("**/draft-*.bpmn")
    }
    into(layout.buildDirectory.dir("bpmn-staging"))
}

// Generate from the staged files
tasks.register<GenerateBpmnModelsTask>("generateFilteredApi") {
    dependsOn("collectBpmnFiles")
    baseDir = layout.buildDirectory.dir("bpmn-staging").get().asFile.toString()
    filePattern = "**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

This is useful when you want to:
- Exclude draft or work-in-progress BPMN files from generation
- Cherry-pick files by naming pattern
- Combine files from multiple source directories into one generation run
