package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Verifies the multi-module promise: a `common` module can expose typed wrappers over the runtime's
 * `ProcessId` / `MessageName` and both service modules can hand it their own generated identifiers
 * without duplicate-class or type-mismatch errors. This is the regression guard for ADR 016.
 */
class MultiModuleRuntimeSharingSmokeTest {

    private val pluginVersion: String = System.getProperty("pluginVersion")
    private val kotlinVersion: String = System.getProperty("kotlinVersion")

    @Test
    fun `common module wrapper accepts ProcessId from two independently generated service APIs`(@TempDir projectDir: File) {

        val commonDir = File(projectDir, "common").also { it.mkdirs() }
        val serviceADir = File(projectDir, "service-a").also { it.mkdirs() }
        val serviceBDir = File(projectDir, "service-b").also { it.mkdirs() }

        copyBpmnResource("c8-subscribe-newsletter.bpmn", File(serviceADir, "src/main/resources/a.bpmn"))
        copyBpmnResource("c8-subscribe-newsletter.bpmn", File(serviceBDir, "src/main/resources/b.bpmn"))

        File(projectDir, "settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            dependencyResolutionManagement {
                repositories {
                    mavenLocal()
                    mavenCentral()
                }
            }
            rootProject.name = "multi-module-smoke"
            include("common", "service-a", "service-b")
            """.trimIndent()
        )

        File(commonDir, "build.gradle.kts").writeText(
            """
            plugins {
                kotlin("jvm") version "$kotlinVersion"
            }
            dependencies {
                implementation("io.github.emaarco:bpmn-to-code-runtime:$pluginVersion")
            }
            """.trimIndent()
        )

        File(commonDir, "src/main/kotlin/com/acme/common/EngineGateway.kt").apply { parentFile.mkdirs() }.writeText(
            """
            package com.acme.common

            import io.github.emaarco.bpmn.runtime.MessageName
            import io.github.emaarco.bpmn.runtime.ProcessId

            class EngineGateway {
                fun start(id: ProcessId): String = "started:" + id
                fun publish(msg: MessageName): String = "published:" + msg
            }
            """.trimIndent()
        )

        writeServiceModule(serviceADir, packagePath = "com.acme.service_a.bpmn", callerName = "UsesApiA")
        writeServiceModule(serviceBDir, packagePath = "com.acme.service_b.bpmn", callerName = "UsesApiB")

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("compileKotlin")
            .build()

        assertThat(result.task(":common:compileKotlin")?.outcome).isIn(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
        assertThat(result.task(":service-a:compileKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":service-b:compileKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun writeServiceModule(moduleDir: File, packagePath: String, callerName: String) {
        File(moduleDir, "build.gradle.kts").writeText(
            """
            plugins {
                kotlin("jvm") version "$kotlinVersion"
                id("io.github.emaarco.bpmn-to-code-gradle") version "$pluginVersion"
            }
            dependencies {
                implementation(project(":common"))
            }
            val generatedSrc = layout.buildDirectory.dir("generated/bpmn")
            tasks.named<io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask>("generateBpmnModelApi") {
                baseDir = projectDir.toString()
                filePattern = "src/main/resources/*.bpmn"
                outputFolderPath = generatedSrc.get().asFile.absolutePath
                packagePath = "$packagePath"
                outputLanguage = io.github.emaarco.bpmn.domain.shared.OutputLanguage.KOTLIN
                processEngine = io.github.emaarco.bpmn.domain.shared.ProcessEngine.ZEEBE
            }
            sourceSets.main { kotlin.srcDir(generatedSrc) }
            tasks.named("compileKotlin") { dependsOn("generateBpmnModelApi") }
            """.trimIndent()
        )

        File(moduleDir, "src/main/kotlin/com/acme/$callerName.kt").apply { parentFile.mkdirs() }.writeText(
            """
            package com.acme

            import com.acme.common.EngineGateway
            import $packagePath.NewsletterSubscriptionProcessApi

            object $callerName {
                fun run(gateway: EngineGateway): String {
                    return gateway.start(NewsletterSubscriptionProcessApi.PROCESS_ID)
                }
            }
            """.trimIndent()
        )
    }

    private fun copyBpmnResource(resourceName: String, destination: File) {
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream("bpmn/$resourceName"))
        destination.parentFile.mkdirs()
        destination.writeBytes(stream.readBytes())
    }
}
