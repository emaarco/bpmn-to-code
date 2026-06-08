plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradlePluginPublish)
    jacoco
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

repositories {
    mavenLocal()
    mavenCentral()
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

val pluginVersionResourceDir = layout.buildDirectory.dir("generated/resources/plugin-version")

val generatePluginVersionResource by tasks.registering {
    val versionProvider = provider { version.toString() }
    val outputDir = pluginVersionResourceDir
    inputs.property("version", versionProvider)
    outputs.dir(outputDir)
    doLast {
        outputDir.get().file("bpmn-to-code-plugin.properties").asFile.apply {
            parentFile.mkdirs()
            writeText("version=${versionProvider.get()}\n")
        }
    }
}

sourceSets.main {
    resources.srcDir(generatePluginVersionResource)
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.bpmnmodel)
    api(libs.kotlinxSerializationJson)
    api(libs.slf4jApi)
    api(libs.kotlinLogging)
    compileOnly(project(":bpmn-to-code-core"))
    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.testing)
    // Core is shaded into the plugin jar at runtime, but the unit tests instantiate the
    // plugin directly and therefore need core on the test runtime classpath. This was
    // previously pulled in transitively via the core test-fixtures dependency.
    testImplementation(project(":bpmn-to-code-core"))
    testImplementation(project(":bpmn-to-code-test-fixtures"))
    testRuntimeOnly(libs.junitPlatformLauncher)
}

// The core is now a Kotlin Multiplatform module; shade its JVM compilation output
// (the `jvmJar` contents) into the plugin jar instead of the old `sourceSets.main` output.
val coreJvmJar = project(":bpmn-to-code-core").tasks.named("jvmJar")

tasks.jar {
    dependsOn(coreJvmJar)
    from({ zipTree(coreJvmJar.get().outputs.files.singleFile) }) {
        exclude("META-INF/MANIFEST.MF")
    }
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    pluginClasspath.from(coreJvmJar)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn("publishToMavenLocal")
    dependsOn(":bpmn-to-code-runtime:publishToMavenLocal")
    systemProperty("pluginVersion", version.toString())
    systemProperty("kotlinVersion", libs.versions.kotlin.get())
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "io.github.emaarco"
            artifactId = "bpmn-to-code-gradle"
        }
    }
    repositories {
        mavenLocal()
    }
}

gradlePlugin {
    website = "https://github.com/emaarco/bpmn-to-code"
    vcsUrl = "https://github.com/emaarco/bpmn-to-code"
    plugins {
        create("io.github.emaarco.bpmn-to-code-gradle") {
            id = "io.github.emaarco.bpmn-to-code-gradle"
            displayName = "bpmn-to-code"
            description =
                "Gradle plugin that bridges gaps between BPMN and code - fostering the creation of clean process-automation solutions"
            implementationClass = "io.github.emaarco.bpmn.adapter.BpmnModelGeneratorPlugin"
            tags = setOf("bpmn", "codegen")
        }
    }
}
