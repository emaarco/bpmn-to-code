@file:OptIn(OpenApiPreview::class)

import io.ktor.plugin.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.2.21"
    id("io.ktor.plugin") version "3.3.2"
    application
}

group = "io.github.emaarco"
version = "0.0.5"

repositories {
    mavenCentral()
}

dependencies {
    // Core dependency - reuse existing logic
    implementation(project(":bpmn-to-code-core"))

    // Ktor server
    implementation("io.ktor:ktor-server-core:3.3.2")
    implementation("io.ktor:ktor-server-netty:3.3.2")
    implementation("io.ktor:ktor-server-content-negotiation:3.3.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.2")
    implementation("io.ktor:ktor-server-cors:3.3.2")
    implementation("io.ktor:ktor-server-call-logging:3.3.2")
    implementation("io.ktor:ktor-server-status-pages:3.3.2")

    // Swagger UI for API documentation
    implementation("io.ktor:ktor-server-swagger:3.3.2")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.21")

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation("io.ktor:ktor-server-test-host:3.3.2")
    testRuntimeOnly(libs.junitPlatformLauncher)
}

ktor {
    fatJar {
        archiveFileName.set("bpmn-to-code-web-${version}-fat.jar")
    }

    @OptIn(OpenApiPreview::class)
    openApi {
        title = "BPMN to Code Web API"
        version = project.version.toString()
        summary = "REST API for generating type-safe process APIs from BPMN files"
        termsOfService = "https://github.com/emaarco/bpmn-to-code"
        contact = "https://github.com/emaarco/bpmn-to-code"
        license = "Apache-2.0"
        description =
            "Upload BPMN files and generate type-safe API definitions for process engines like Camunda 7 and Zeebe. " +
                    "Supports both Java and Kotlin output languages with optional API versioning."

        // Location of the generated specification (defaults to openapi/generated.json)
        target = project.layout.buildDirectory.file("openapi/generated.json")
    }
}

// Enable zip64 for shadow JAR (required for archives with >65535 entries)
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    isZip64 = true
}

application {
    mainClass.set("io.github.emaarco.bpmn.web.ApplicationKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Docker tasks
val dockerImageName = "emaarco/bpmn-to-code-web"
val dockerImageTag = project.version.toString()

fun findDocker(): String {
    return try {
        val result = providers.exec { commandLine("which", "docker") }.standardOutput.asText.get().trim()
        result.ifEmpty { "docker" }
    } catch (_: Exception) {
        "docker"
    }
}

tasks.register<Exec>("dockerBuild") {
    group = "docker"
    description = "Build Docker image for bpmn-to-code-web"

    dependsOn("buildFatJar")
    workingDir = rootProject.projectDir

    doFirst {
        val docker = findDocker()
        println("Building Docker image: $dockerImageName:$dockerImageTag")
        println("Using docker: $docker")
        commandLine(
            docker, "build",
            "--platform", "linux/amd64",
            "-f", "bpmn-to-code-web/Dockerfile",
            "-t", "$dockerImageName:$dockerImageTag",
            "-t", "$dockerImageName:latest",
            "."
        )
    }
}

tasks.register<Exec>("dockerPush") {
    group = "docker"
    description = "Push Docker image to Docker Hub"

    dependsOn("dockerBuild")
    workingDir = rootProject.projectDir

    doFirst {
        commandLine(findDocker(), "push", "$dockerImageName:$dockerImageTag")
    }

    doLast {
        println("Pushed image: $dockerImageName:$dockerImageTag")
    }
}

tasks.register<Exec>("dockerRun") {
    group = "docker"
    description = "Run Docker container locally on port 8080"

    dependsOn("dockerBuild")
    workingDir = rootProject.projectDir

    doFirst {
        commandLine(
            findDocker(), "run",
            "-p", "9099:8080",
            "--rm",
            "-d",
            "$dockerImageName:$dockerImageTag"
        )
    }
}