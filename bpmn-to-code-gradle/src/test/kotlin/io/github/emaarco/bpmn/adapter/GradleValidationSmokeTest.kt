package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class GradleValidationSmokeTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "ZEEBE, c8-subscribe-newsletter.bpmn",
        "CAMUNDA_7, c7-subscribe-newsletter.bpmn",
        "OPERATON, operaton-subscribe-newsletter.bpmn",
    )
    fun `validateBpmnModels succeeds for valid BPMN files`(
        engine: String,
        bpmnFile: String,
        @TempDir projectDir: File,
    ) {
        // given: a minimal project with a valid BPMN file for the given engine
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = javaClass.classLoader.getResourceAsStream("bpmn/$bpmnFile")!!
        File(resourcesDir, bpmnFile).writeBytes(bpmnStream.readBytes())
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'io.github.emaarco.bpmn-to-code-gradle'
            }

            tasks.named('validateBpmnModels') {
                baseDir = projectDir.toString()
                filePattern = 'src/main/resources/*.bpmn'
                processEngine = io.github.emaarco.bpmn.domain.shared.ProcessEngine.$engine
            }
            """.trimIndent()
        )

        // when: running the validateBpmnModels task
        val result = GradleRunner.create()
            .withJacocoAgent(projectDir)
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("validateBpmnModels")
            .build()

        // then: the task succeeds with a validation passed message
        assertThat(result.task(":validateBpmnModels")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("BPMN validation passed")
    }
}
