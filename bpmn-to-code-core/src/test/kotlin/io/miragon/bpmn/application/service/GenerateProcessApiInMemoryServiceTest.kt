package io.miragon.bpmn.application.service

import io.miragon.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.miragon.bpmn.application.port.outbound.ExtractBpmnPort
import io.miragon.bpmn.application.port.outbound.GenerateApiCodePort
import io.miragon.bpmn.domain.BpmnModel
import io.miragon.bpmn.domain.GeneratedApiFile
import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.BpmnValidationException
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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

    @Test
    fun `service rejects a model that targets a different engine before generating`() {

        // given: a model detected as Camunda 7 but generation requested for Operaton
        val bpmnInput = GenerateProcessApiInMemoryUseCase.BpmnInput(
            bpmnXml = "<bpmn>camunda</bpmn>",
            processName = "newsletter.bpmn"
        )
        every { bpmnService.extract(any(), any()) } returns dummyModel.copy(detectedEngine = ProcessEngine.CAMUNDA_7)
        val command = GenerateProcessApiInMemoryUseCase.Command(
            bpmnContents = listOf(bpmnInput),
            packagePath = "com.example",
            outputLanguage = OutputLanguage.KOTLIN,
            engine = ProcessEngine.OPERATON
        )

        // when / then: it fails with a single engine-mismatch error and never generates code
        assertThatThrownBy { underTest.generateProcessApi(command) }
            .isInstanceOf(BpmnValidationException::class.java)
            .extracting("violations")
            .matches { (it as List<*>).size == 1 }
        verify(exactly = 0) { codeGenerator.generateCode(any()) }
    }

    private val dummyModel = BpmnModel(
        processId = "testProcess",
        flowNodes = emptyList(),
        messages = emptyList(),
        signals = emptyList(),
        errors = emptyList(),
    )
}
