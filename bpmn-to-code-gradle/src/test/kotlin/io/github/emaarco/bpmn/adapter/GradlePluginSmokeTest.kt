package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class GradlePluginSmokeTest {

    private val kotlinVersion: String = requireNotNull(System.getProperty("kotlinVersion")) {
        "kotlinVersion system property must be set — run tests via Gradle"
    }

    @ParameterizedTest(name = "{0} / {1}")
    @CsvSource(
        "ZEEBE, KOTLIN, c8-subscribe-newsletter.bpmn",
        "CAMUNDA_7, KOTLIN, c7-subscribe-newsletter.bpmn",
        "OPERATON, KOTLIN, operaton-subscribe-newsletter.bpmn",
        "ZEEBE, JAVA, c8-subscribe-newsletter.bpmn",
    )
    fun `generateBpmnModelApi produces output files that compile`(
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

        val isKotlin = language == "KOTLIN"
        val languagePlugin = if (isKotlin) "id 'org.jetbrains.kotlin.jvm' version '$kotlinVersion'" else "id 'java'"
        val compileTask = if (isKotlin) "compileKotlin" else "compileJava"
        val srcDirBlock = if (isKotlin) "kotlin { srcDirs = ['build/generated'] }" else "java { srcDir 'build/generated' }"

        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                $languagePlugin
                id 'io.github.emaarco.bpmn-to-code-gradle'
            }
            repositories {
                mavenLocal()
                mavenCentral()
            }
            sourceSets {
                main {
                    $srcDirBlock
                }
            }
            tasks.named('$compileTask') {
                dependsOn tasks.named('generateBpmnModelApi')
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

        // when: running the compile task (which depends on generateBpmnModelApi)
        val result = GradleRunner.create()
            .withJacocoAgent()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(compileTask)
            .build()

        // then: both generation and compilation succeed
        assertThat(result.task(":generateBpmnModelApi")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":$compileTask")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val packageDir = File(projectDir, "build/generated/io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = requireNotNull(packageDir.listFiles())
        assertThat(generatedFiles).isNotEmpty()
        val expectedExt = if (isKotlin) ".kt" else ".java"
        assertThat(generatedFiles).allSatisfy { file -> assertThat(file.isFile).isTrue() }
        assertThat(generatedFiles).allSatisfy { file -> assertThat(file.name).endsWith(expectedExt) }
    }
}
