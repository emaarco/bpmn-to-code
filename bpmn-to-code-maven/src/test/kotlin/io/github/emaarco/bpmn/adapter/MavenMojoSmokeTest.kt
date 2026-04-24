package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class MavenMojoSmokeTest {

    @ParameterizedTest(name = "{0} / {1}")
    @CsvSource(
        "ZEEBE, KOTLIN, c8-subscribe-newsletter.bpmn",
        "CAMUNDA_7, KOTLIN, c7-subscribe-newsletter.bpmn",
        "OPERATON, KOTLIN, operaton-subscribe-newsletter.bpmn",
        "ZEEBE, JAVA, c8-subscribe-newsletter.bpmn",
    )
    fun `mojo generates output files`(
        engine: String,
        language: String,
        bpmnFile: String,
        @TempDir projectDir: File,
    ) {
        // given: a temp project directory with a BPMN resource and a configured mojo
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = requireNotNull(javaClass.classLoader.getResourceAsStream("bpmn/$bpmnFile"))
        File(resourcesDir, bpmnFile).writeBytes(bpmnStream.readBytes())
        val outputDir = File(projectDir, "build/generated")
        val mojo = BpmnModelMojo()
        setField(mojo, "baseDir", projectDir.absolutePath)
        setField(mojo, "filePattern", "src/main/resources/*.bpmn")
        setField(mojo, "outputFolderPath", outputDir.absolutePath)
        setField(mojo, "packagePath", "io.github.emaarco.smoketest")
        setField(mojo, "outputLanguage", language)
        setField(mojo, "processEngine", engine)

        // when: executing the mojo
        mojo.execute()

        // then: only ProcessApi files are generated (shared types ship via runtime artifact)
        val packageDir = File(outputDir, "io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = requireNotNull(packageDir.listFiles())
        assertThat(generatedFiles).isNotEmpty()
        val expectedExt = if (language == "KOTLIN") ".kt" else ".java"
        assertThat(generatedFiles).allSatisfy { file -> assertThat(file.isFile).isTrue() }
        assertThat(generatedFiles).allSatisfy { file -> assertThat(file.name).endsWith(expectedExt) }
    }

    private fun setField(obj: Any, name: String, value: Any) {
        val field = obj.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(obj, value)
    }
}
