plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    application
}

group = "io.github.emaarco"
version = "0.0.19"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":bpmn-to-code-core"))
    implementation(libs.mcpKotlinSdk)
    implementation(libs.kotlinLogging)
    implementation(libs.logbackClassic)
    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

application {
    mainClass.set("io.github.emaarco.bpmn.mcp.ApplicationKt")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    isZip64 = true
    archiveClassifier.set("fat")
    mergeServiceFiles()
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Docker tasks
val dockerImageName = "emaarco/bpmn-to-code-mcp"
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
    description = "Build Docker image for bpmn-to-code-mcp"

    dependsOn("shadowJar")
    workingDir = rootProject.projectDir

    doFirst {
        val docker = findDocker()
        println("Building Docker image: $dockerImageName:$dockerImageTag")
        commandLine(
            docker, "build",
            "--platform", "linux/amd64",
            "-f", "bpmn-to-code-mcp/Dockerfile",
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
            "-p", "8080:8080",
            "--rm",
            "$dockerImageName:$dockerImageTag"
        )
    }
}
