plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradlePluginPublish)
}

group = "io.github.emaarco"
version = "0.0.3-alpha"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.bpmnmodel)
    api(libs.bundles.codegen)
    compileOnly(project(":bpmn-to-code-core"))
    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.jar {
    from(project(":bpmn-to-code-core").sourceSets.main.get().output)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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
