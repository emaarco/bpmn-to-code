package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class GradlePluginSmokeTest {

    @ParameterizedTest(name = "{0} / {1}")
    @CsvSource(
        "ZEEBE, KOTLIN, c8-subscribe-newsletter.bpmn",
        "CAMUNDA_7, KOTLIN, c7-subscribe-newsletter.bpmn",
        "OPERATON, KOTLIN, operaton-subscribe-newsletter.bpmn",
        "ZEEBE, JAVA, c8-subscribe-newsletter.bpmn",
    )
    fun `generateBpmnModelApi produces output files`(
        engine: String,
        language: String,
        bpmnFile: String,
        @TempDir projectDir: File,
    ) {
        // given: a minimal Gradle project with the plugin applied and a BPMN resource
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = requireNotNull(javaClass.classLoader.getResourceAsStream("bpmn/$bpmnFile"))
        File(resourcesDir, bpmnFile).writeBytes(bpmnStream.readBytes())
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'io.github.emaarco.bpmn-to-code-gradle'
            }

            tasks.named('generateBpmnModelApi') {
                baseDir = projectDir.toString()
                filePattern = 'src/main/resources/*.bpmn'
                outputFolderPath = "${'$'}{projectDir}/build/generated"
                packagePath = 'io.github.emaarco.smoketest'
                outputLanguage = io.github.emaarco.bpmn.domain.shared.OutputLanguage.$language
                processEngine = io.github.emaarco.bpmn.domain.shared.ProcessEngine.$engine
            }
            """.trimIndent()
        )

        // when: running the generateBpmnModelApi task
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateBpmnModelApi")
            .build()

        // then: the task succeeds and generates the expected files
        assertThat(result.task(":generateBpmnModelApi")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val packageDir = File(projectDir, "build/generated/io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = requireNotNull(packageDir.listFiles())
        assertThat(generatedFiles).isNotEmpty()
        val expectedExt = if (language == "KOTLIN") ".kt" else ".java"
        val modelFiles = generatedFiles.filter { it.isFile }
        assertThat(modelFiles).allSatisfy { file -> assertThat(file.name).endsWith(expectedExt) }
        val typesDir = generatedFiles.first { it.isDirectory && it.name == "types" }
        assertThat(requireNotNull(typesDir.listFiles())).allSatisfy { file -> assertThat(file.name).endsWith(expectedExt) }
    }
}
