package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Resolves the plugin from mavenLocal (published POM) instead of using withPluginClasspath().
 * This catches missing transitive dependencies that wouldn't surface with classpath-based tests.
 * See: https://github.com/emaarco/bpmn-to-code/issues/159
 *
 * Agent note: If this test fails, check whether the missing dependency affects only the Gradle
 * plugin module, only the Maven plugin module, or both — since they share the same dependency
 * structure (fat jar with core classes + declared transitive deps in POM). A fix here likely
 * needs to be mirrored in bpmn-to-code-maven/build.gradle.kts as well.
 */
class GradlePluginDependencyResolutionSmokeTest {

    private val pluginVersion: String = System.getProperty("pluginVersion")

    @Test
    fun `plugin resolves all dependencies from published artifact`(@TempDir projectDir: File) {
        // Copy BPMN file into the temp project
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = javaClass.classLoader.getResourceAsStream("bpmn/c8-newsletter.bpmn")!!
        File(resourcesDir, "c8-newsletter.bpmn").writeBytes(bpmnStream.readBytes())

        // Write settings.gradle with pluginManagement resolving from mavenLocal
        File(projectDir, "settings.gradle").writeText(
            """
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            """.trimIndent()
        )

        // Write build.gradle that applies the plugin by ID + version (resolved from mavenLocal)
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'io.github.emaarco.bpmn-to-code-gradle' version '$pluginVersion'
            }

            tasks.named('generateBpmnModelApi') {
                baseDir = projectDir.toString()
                filePattern = 'src/main/resources/*.bpmn'
                outputFolderPath = "${'$'}{projectDir}/build/generated"
                packagePath = 'io.github.emaarco.smoketest'
                outputLanguage = io.github.emaarco.bpmn.domain.shared.OutputLanguage.KOTLIN
                processEngine = io.github.emaarco.bpmn.domain.shared.ProcessEngine.ZEEBE
                useVersioning = false
            }
            """.trimIndent()
        )

        // Run WITHOUT withPluginClasspath() — Gradle resolves from mavenLocal
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateBpmnModelApi")
            .build()

        assertThat(result.task(":generateBpmnModelApi")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val packageDir = File(projectDir, "build/generated/io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = packageDir.listFiles()!!
        assertThat(generatedFiles).isNotEmpty()
        assertThat(generatedFiles).allSatisfy { file ->
            assertThat(file.name).endsWith(".kt")
        }
    }
}
