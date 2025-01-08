package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

// TODO: warning & content
class Camunda7ModelExtractorTest {

    private val underTest = Camunda7ModelExtractor()

    @Test
    fun `extract returns valid BpmnModel`() {
        val file = File(javaClass.getResource("/bpmn/c7-newsletter.bpmn").toURI())
        val bpmnModel = underTest.extract(file)
        assertThat(bpmnModel).isNotNull()
    }
}
