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

        // given: a minimal project configured to resolve the plugin from mavenLocal
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = requireNotNull(javaClass.classLoader.getResourceAsStream("bpmn/c8-subscribe-newsletter.bpmn"))
        File(resourcesDir, "c8-subscribe-newsletter.bpmn").writeBytes(bpmnStream.readBytes())
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
            }
            """.trimIndent()
        )

        // when: running WITHOUT withPluginClasspath() so Gradle resolves from mavenLocal
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateBpmnModelApi")
            .build()

        // then: the task succeeds and generates Kotlin files
        assertThat(result.task(":generateBpmnModelApi")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val packageDir = File(projectDir, "build/generated/io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = requireNotNull(packageDir.listFiles())
        assertThat(generatedFiles).isNotEmpty()
        val modelFiles = generatedFiles.filter { it.isFile }
        assertThat(modelFiles).allSatisfy { file -> assertThat(file.name).endsWith(".kt") }
        val typesDir = generatedFiles.first { it.isDirectory && it.name == "types" }
        assertThat(requireNotNull(typesDir.listFiles())).allSatisfy { file -> assertThat(file.name).endsWith(".kt") }
    }

    @Test
    fun `generateBpmnModelJson resolves kotlinx-serialization from published artifact`(@TempDir projectDir: File) {

        // given: a minimal project configured to resolve the plugin from mavenLocal
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = requireNotNull(javaClass.classLoader.getResourceAsStream("bpmn/c8-subscribe-newsletter.bpmn"))
        File(resourcesDir, "c8-subscribe-newsletter.bpmn").writeBytes(bpmnStream.readBytes())
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
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'io.github.emaarco.bpmn-to-code-gradle' version '$pluginVersion'
            }

            tasks.named('generateBpmnModelJson') {
                baseDir = projectDir.toString()
                filePattern = 'src/main/resources/*.bpmn'
                outputFolderPath = "${'$'}{projectDir}/build/generated-json"
                processEngine = io.github.emaarco.bpmn.domain.shared.ProcessEngine.ZEEBE
            }
            """.trimIndent()
        )

        // when: running WITHOUT withPluginClasspath() so Gradle resolves from mavenLocal
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateBpmnModelJson")
            .build()

        // then: the task succeeds and generates JSON files
        assertThat(result.task(":generateBpmnModelJson")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val outputDir = File(projectDir, "build/generated-json")
        assertThat(outputDir).isDirectory()
        val generatedFiles = requireNotNull(outputDir.listFiles())
        assertThat(generatedFiles).isNotEmpty()
        assertThat(generatedFiles).allSatisfy { file -> assertThat(file.name).endsWith(".json") }
    }
}
