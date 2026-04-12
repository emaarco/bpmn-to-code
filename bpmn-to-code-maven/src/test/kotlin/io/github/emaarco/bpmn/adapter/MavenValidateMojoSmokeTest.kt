package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class MavenValidateMojoSmokeTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "ZEEBE, c8-subscribe-newsletter.bpmn",
        "CAMUNDA_7, c7-subscribe-newsletter.bpmn",
        "OPERATON, operaton-subscribe-newsletter.bpmn",
    )
    fun `mojo validates BPMN files without errors`(
        engine: String,
        bpmnFile: String,
        @TempDir projectDir: File,
    ) {
        // Copy BPMN file into the temp project
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = javaClass.classLoader.getResourceAsStream("bpmn/$bpmnFile")!!
        File(resourcesDir, bpmnFile).writeBytes(bpmnStream.readBytes())

        // Instantiate Mojo and set fields via reflection
        val mojo = BpmnValidateMojo()
        setField(mojo, "baseDir", projectDir.absolutePath)
        setField(mojo, "filePattern", "src/main/resources/*.bpmn")
        setField(mojo, "processEngine", engine)
        setField(mojo, "failOnWarning", false)

        // Execute — should not throw
        assertThatCode { mojo.execute() }.doesNotThrowAnyException()
    }

    private fun setField(obj: Any, name: String, value: Any) {
        val field = obj.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(obj, value)
    }
}
