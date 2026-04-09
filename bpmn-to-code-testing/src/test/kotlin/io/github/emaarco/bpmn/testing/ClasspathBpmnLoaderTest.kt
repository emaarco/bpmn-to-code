package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ClasspathBpmnLoaderTest {

    @Test
    fun `loads bpmn files from classpath directory`() {
        val resources = ClasspathBpmnLoader.load("processes", ProcessEngine.CAMUNDA_7)
        assertThat(resources).isNotEmpty
        assertThat(resources).allMatch { it.fileName.endsWith(".bpmn") }
    }

    @Test
    fun `sets the correct engine on loaded resources`() {
        val resources = ClasspathBpmnLoader.load("processes", ProcessEngine.ZEEBE)
        assertThat(resources).allMatch { it.engine == ProcessEngine.ZEEBE }
    }

    @Test
    fun `throws on missing classpath path`() {
        assertThatThrownBy {
            ClasspathBpmnLoader.load("nonexistent/path", ProcessEngine.CAMUNDA_7)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No classpath resources found")
    }

    @Test
    fun `handles trailing slash in path`() {
        val resources = ClasspathBpmnLoader.load("processes/", ProcessEngine.CAMUNDA_7)
        assertThat(resources).isNotEmpty
    }
}
