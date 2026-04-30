plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
    jacoco
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

repositories {
    mavenCentral()
}

dependencies {
    // Core dependency - reuse existing logic
    implementation(project(":bpmn-to-code-core"))

    // Ktor dependencies
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.logbackClassic)
    implementation(libs.kotlinLogging)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(testFixtures(project(":bpmn-to-code-core")))
    testRuntimeOnly(libs.junitPlatformLauncher)
}

ktor {
    fatJar {
        archiveFileName.set("bpmn-to-code-web-${version}-fat.jar")
    }


}

// Enable zip64 for shadow JAR (required for archives with >65535 entries)
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    isZip64 = true
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

val copyLibrarySources by tasks.registering(Copy::class) {
    val runtimeProject = project(":bpmn-to-code-runtime")
    from(runtimeProject.layout.projectDirectory.dir("src/main/kotlin/io/github/emaarco/bpmn/runtime"))
    into(layout.buildDirectory.dir("generated/resources/library-sources/library-sources"))
    include("*.kt")
}

val generateLibrarySourcesManifest by tasks.registering {
    dependsOn(copyLibrarySources)
    val outputDir = layout.buildDirectory.dir("generated/resources/library-sources/library-sources")
    outputs.file(outputDir.map { it.file("manifest.txt") })
    doLast {
        val dir = outputDir.get().asFile
        val names = dir.listFiles { f -> f.isFile && f.name.endsWith(".kt") }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
        dir.resolve("manifest.txt").writeText(names.joinToString("\n") + "\n")
    }
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/resources/library-sources"))
}

tasks.named("processResources") {
    dependsOn(generateLibrarySourcesManifest)
}

application {
    mainClass.set("io.github.emaarco.bpmn.web.ApplicationKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

private val coverageExclusions = listOf(
    "**/routes/**",           // thin Ktor adapter layer — integration-tested separately
    "**/web/Application*",    // application entry point / wiring
    "**/web/config/**",       // pure configuration data classes
    "**/model/ConfigResponse*",
)

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}

tasks.named<ProcessResources>("processResources") {
    val projectVersion = project.version.toString()
    filesMatching("version.properties") {
        expand("projectVersion" to projectVersion)
    }
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
        // Remove old tags before building so they don't become <none>:<none> dangling images.
        // isIgnoreExitValue = true is the Gradle equivalent of "|| true": if the image doesn't
        // exist yet (first-ever build), docker rmi exits non-zero — we want to continue anyway.
        val docker = findDocker()
        listOf("$dockerImageName:$dockerImageTag", "$dockerImageName:latest").forEach { tag ->
            project.exec {
                commandLine(docker, "rmi", tag)
                isIgnoreExitValue = true
            }
        }
    }

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