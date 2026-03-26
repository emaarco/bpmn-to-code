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
        "ZEEBE, KOTLIN, c8-newsletter.bpmn",
        "CAMUNDA_7, KOTLIN, c7-newsletter.bpmn",
        "OPERATON, KOTLIN, operaton-newsletter.bpmn",
        "ZEEBE, JAVA, c8-newsletter.bpmn",
    )
    fun `generateBpmnModelApi produces output files`(
        engine: String,
        language: String,
        bpmnFile: String,
        @TempDir projectDir: File,
    ) {
        // Copy BPMN file into the temp project
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = javaClass.classLoader.getResourceAsStream("bpmn/$bpmnFile")!!
        File(resourcesDir, bpmnFile).writeBytes(bpmnStream.readBytes())

        // Write settings.gradle
        File(projectDir, "settings.gradle").writeText("")

        // Write build.gradle (Groovy DSL — avoids classpath issues with typed imports)
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
                useVersioning = false
            }
            """.trimIndent()
        )

        // Run the task
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateBpmnModelApi")
            .build()

        // Verify task succeeded
        assertThat(result.task(":generateBpmnModelApi")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        // Verify output files were generated
        val packageDir = File(projectDir, "build/generated/io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = packageDir.listFiles()!!
        assertThat(generatedFiles).isNotEmpty()

        val expectedExt = if (language == "KOTLIN") ".kt" else ".java"
        assertThat(generatedFiles).allSatisfy { file ->
            assertThat(file.name).endsWith(expectedExt)
        }
    }
}
