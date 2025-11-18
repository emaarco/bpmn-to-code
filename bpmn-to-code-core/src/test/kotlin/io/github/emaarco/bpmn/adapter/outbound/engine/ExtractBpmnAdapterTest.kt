package io.github.emaarco.bpmn.adapter.outbound.engine

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.EngineSpecificExtractor
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File

class ExtractBpmnAdapterTest {

    private val extractor = mockk<EngineSpecificExtractor>()
    private val underTest = ExtractBpmnAdapter(
        extractors = mapOf(ProcessEngine.ZEEBE to extractor)
    )

    @Test
    fun `extract returns model using correct extractor`() {

        // given: a dummy extractor that returns an expected BpmnModel
        val expectedModel = testBpmnModel(processId = "dummyProcess")
        val dummyFile = File.createTempFile("dummy", ".bpmn").apply { deleteOnExit() }
        val inputStream = dummyFile.inputStream()
        val bpmnFile = BpmnResource(fileName = "dummy.bpmn", content = inputStream, engine = ProcessEngine.ZEEBE)
        every { extractor.extract(any()) } returns expectedModel

        // when: extract is invoked
        val result = underTest.extract(bpmnFile)

        // then: the extractor is used and the expected model is returned
        verify { extractor.extract(any()) }
        assertThat(result).isEqualTo(expectedModel)
    }

    @Test
    fun `throws exception when no extractor found for engine`() {
        val dummyFile = File.createTempFile("dummy", ".bpmn").apply { deleteOnExit() }
        val inputStream = dummyFile.inputStream()
        assertThrows(IllegalStateException::class.java) {
            underTest.extract(
                BpmnResource(
                    fileName = "dummy.bpmn",
                    content = inputStream,
                    engine = ProcessEngine.CAMUNDA_7
                )
            )
        }
    }
}
