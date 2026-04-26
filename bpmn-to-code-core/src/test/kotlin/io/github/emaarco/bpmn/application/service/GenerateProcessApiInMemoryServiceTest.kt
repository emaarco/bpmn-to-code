package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateProcessApiInMemoryServiceTest {

    private val codeGenerator = mockk<GenerateApiCodePort>(relaxed = true)
    private val bpmnService = mockk<ExtractBpmnPort>(relaxed = true)

    private val underTest = GenerateProcessApiInMemoryService(
        codeGenerator = codeGenerator,
        bpmnService = bpmnService
    )

    @Test
    fun `service generates API files from BPMN content`() {

        // given: BPMN content
        val bpmnInput = GenerateProcessApiInMemoryUseCase.BpmnInput(
            bpmnXml = "<bpmn>test</bpmn>",
            processName = "test.bpmn"
        )
        val expectedGeneratedFile = GeneratedApiFile(
            fileName = "TestProcessApi.kt",
            packagePath = "com.example",
            content = "// generated code",
            language = OutputLanguage.KOTLIN,
            processId = "test",
        )
        every { bpmnService.extract(any(), any()) } returns dummyModel
        every { codeGenerator.generateCode(any()) } returns listOf(expectedGeneratedFile)
        val command = GenerateProcessApiInMemoryUseCase.Command(
            bpmnContents = listOf(bpmnInput),
            packagePath = "com.example",
            outputLanguage = OutputLanguage.KOTLIN,
            engine = ProcessEngine.ZEEBE
        )

        // when: generateProcessApi is called
        val result = underTest.generateProcessApi(command)

        // then: BpmnFile is created, models are extracted, code is generated
        verify { bpmnService.extract(match { it.fileName == "test.bpmn" }, eq(ProcessEngine.ZEEBE)) }
        verify { codeGenerator.generateCode(match { it.model.processId == dummyModel.processId }) }
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(expectedGeneratedFile)
        confirmVerified(codeGenerator, bpmnService)
    }

    private val dummyModel = BpmnModel(
        processId = "testProcess",
        flowNodes = emptyList(),
        messages = emptyList(),
        signals = emptyList(),
        errors = emptyList(),
    )
}
