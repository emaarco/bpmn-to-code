package io.miragon.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DeprecatedPluginIdSmokeTest {

    @Test
    fun `old plugin id applies the new plugin and warns`(@TempDir projectDir: File) {
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.github.emaarco.bpmn-to-code-gradle'
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--group", "BPMN")
            .build()

        // then: the build configures (old id resolves), the deprecation warning is shown,
        // and the real plugin's tasks are registered.
        assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains(
            "Plugin id 'io.github.emaarco.bpmn-to-code-gradle' is deprecated",
            "switch to 'io.miragon.bpmn-to-code-gradle'",
        )
        assertThat(result.output).contains("generateBpmnModelApi")
    }
}
