package io.github.emaarco.bpmn.adapter.outbound.engine

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.EngineSpecificExtractor
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class ExtractBpmnAdapterTest {

    private val extractor = mockk<EngineSpecificExtractor>(relaxed = true)
    private val underTest = ExtractBpmnAdapter(
        extractors = mapOf(ProcessEngine.ZEEBE to extractor)
    )

    @Test
    fun `extract returns model using the correct extractor`() {

        // given: a dummy BPMN resource and a stubbed extractor
        val expectedModel = testBpmnModel(processId = "dummyProcess")
        val tempFile = File.createTempFile("dummy", ".bpmn").apply { deleteOnExit() }
        val bpmnResource = BpmnResource(
            fileName = "dummy.bpmn",
            content = tempFile.inputStream(),
        )
        every { extractor.extract(any()) } returns expectedModel

        // when: extracting with a supported engine
        val result = underTest.extract(bpmnFile = bpmnResource, engine = ProcessEngine.ZEEBE)

        // then: the extractor is called and the model is returned
        verify { extractor.extract(any()) }
        assertThat(result).isEqualTo(expectedModel)
    }

    @Test
    fun `extract throws when no extractor is registered for the engine`() {

        // given: a resource targeting an engine with no registered extractor
        val tempFile = File.createTempFile("dummy", ".bpmn").apply { deleteOnExit() }
        val bpmnResource = BpmnResource(
            fileName = "dummy.bpmn",
            content = tempFile.inputStream(),
        )

        // when / then: an exception is thrown
        assertThatThrownBy { underTest.extract(bpmnFile = bpmnResource, engine = ProcessEngine.CAMUNDA_7) }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
