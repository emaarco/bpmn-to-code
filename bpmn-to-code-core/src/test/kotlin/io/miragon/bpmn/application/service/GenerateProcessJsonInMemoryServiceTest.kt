package io.miragon.bpmn.application.service

import io.miragon.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.miragon.bpmn.application.port.outbound.ExtractBpmnPort
import io.miragon.bpmn.application.port.outbound.GenerateJsonPort
import io.miragon.bpmn.domain.GeneratedJsonFile
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.testBpmnModel
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateProcessJsonInMemoryServiceTest {

    private val jsonGenerator = mockk<GenerateJsonPort>(relaxed = true)
    private val bpmnExtractor = mockk<ExtractBpmnPort>(relaxed = true)

    private val underTest = GenerateProcessJsonInMemoryService(
        jsonGenerator = jsonGenerator,
        bpmnExtractor = bpmnExtractor,
    )

    @Test
    fun `generateProcessJson generates JSON files from BPMN content`() {

        // given: BPMN content and a mock extractor
        val bpmnInput = GenerateProcessJsonInMemoryUseCase.BpmnInput(
            bpmnXml = "<bpmn>test</bpmn>",
            processName = "test.bpmn",
        )
        val expectedJsonFile = GeneratedJsonFile(fileName = "order.json", content = "{}")
        every { bpmnExtractor.extract(any(), any()) } returns dummyModel
        every { jsonGenerator.generateJson(any()) } returns expectedJsonFile
        val command = GenerateProcessJsonInMemoryUseCase.Command(
            bpmnContents = listOf(bpmnInput),
            engine = ProcessEngine.ZEEBE,
        )

        // when: generateProcessJson is called
        val result = underTest.generateProcessJson(command)

        // then: BPMN is extracted, JSON is generated and returned
        verify { bpmnExtractor.extract(match { it.fileName == "test.bpmn" }, eq(ProcessEngine.ZEEBE)) }
        verify { jsonGenerator.generateJson(any()) }
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(expectedJsonFile)
        confirmVerified(jsonGenerator, bpmnExtractor)
    }

    private val dummyModel = testBpmnModel()
}
