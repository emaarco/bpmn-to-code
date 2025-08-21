import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

plugins {
    kotlin("jvm") version "1.9.25"
    id("io.github.emaarco.bpmn-to-code-gradle") version "0.0.9"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// Task for Camunda 7 BPMN models
tasks.register<GenerateBpmnModelsTask>("generateBpmnModelApiForC7") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/c7/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "io.github.emaarco.c7"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.CAMUNDA_7
    useVersioning = false
}

// Task for Zeebe BPMN models
tasks.register<GenerateBpmnModelsTask>("generateBpmnModelApiForZeebe") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/c8/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "io.github.emaarco.c8"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = true
}

// Aggregate task to run both tasks together
tasks.register("generateBpmnModels") {
    dependsOn("generateBpmnModelApiForC7", "generateBpmnModelApiForZeebe")
}