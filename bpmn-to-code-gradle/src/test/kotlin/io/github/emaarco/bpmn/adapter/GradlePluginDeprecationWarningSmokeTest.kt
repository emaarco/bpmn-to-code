package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GradlePluginDeprecationWarningSmokeTest {

    @Test
    fun `applying the plugin logs the io_miragon deprecation warning`(
        @TempDir projectDir: File,
    ) {
        // given: a minimal Gradle project that only applies the plugin
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'io.github.emaarco.bpmn-to-code-gradle'
            }
            """.trimIndent()
        )

        // when: configuring the project (any task triggers plugin apply)
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("help")
            .build()

        // then: the apply-time deprecation warning is printed
        assertThat(result.output)
            .contains("bpmn-to-code will be moved to the io.miragon namespace")
            .contains("io.github.emaarco")
            .contains("https://github.com/miragon/bpmn-to-code")
    }
}
