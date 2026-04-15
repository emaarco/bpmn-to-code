package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class GenerateProcessJsonServiceTest {

    private val jsonGenerator = mockk<GenerateJsonPort>(relaxed = true)
    private val bpmnFileLoader = mockk<LoadBpmnFilesPort>(relaxed = true)
    private val bpmnExtractor = mockk<ExtractBpmnPort>(relaxed = true)
    private val fileSaver = mockk<SaveProcessJsonPort>(relaxed = true)

    private val underTest = GenerateProcessJsonService(
        jsonGenerator = jsonGenerator,
        bpmnFileLoader = bpmnFileLoader,
        bpmnExtractor = bpmnExtractor,
        fileSaver = fileSaver,
    )

    @Test
    fun `generateProcessJson generates JSON and writes to disk`() {

        // given: a dummy BPMN resource and a command
        val dummyResource = BpmnResource(fileName = "dummy.bpmn", content = "<bpmn></bpmn>".byteInputStream())
        val expectedJsonFile = GeneratedJsonFile(fileName = "order.json", content = "{}")
        every { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") } returns listOf(dummyResource)
        every { bpmnExtractor.extract(any(), any()) } returns dummyModel
        every { jsonGenerator.generateJson(any()) } returns expectedJsonFile
        val command = GenerateProcessJsonFromFilesystemUseCase.Command(
            baseDir = "baseDir",
            filePattern = "*.bpmn",
            engine = ProcessEngine.ZEEBE,
            outputFolderPath = "outputFolder",
        )

        // when: generateProcessJson is invoked
        underTest.generateProcessJson(command)

        // then: JSON is generated and written to disk
        verify { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") }
        verify { jsonGenerator.generateJson(any()) }
        verify { fileSaver.writeFiles(listOf(expectedJsonFile), "outputFolder") }
        confirmVerified(jsonGenerator, bpmnFileLoader, fileSaver)
    }

    private val dummyModel = testBpmnModel()
}
