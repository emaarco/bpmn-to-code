package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class GenerateProcessApiServiceTest {

    private val codeGenerator = mockk<GenerateApiCodePort>(relaxed = true)
    private val bpmnFileLoader = mockk<LoadBpmnFilesPort>(relaxed = true)
    private val bpmnService = mockk<ExtractBpmnPort>(relaxed = true)
    private val fileSystemOutput = mockk<ProcessApiFileSaver>(relaxed = true)

    private val underTest = GenerateProcessApiService(
        codeGenerator = codeGenerator,
        bpmnFileLoader = bpmnFileLoader,
        bpmnService = bpmnService,
        fileSystemOutput = fileSystemOutput
    )

    @Test
    fun `generateProcessApi generates API file`() {

        // given: a dummy BPMN resource and a command
        val dummyResource = BpmnResource(
            fileName = "dummy.bpmn",
            content = "<bpmn></bpmn>".encodeToByteArray(),
        )
        val expectedGeneratedFile = GeneratedApiFile(
            fileName = "NewsletterSubscriptionProcessApi.kt",
            packagePath = "de.emaarco.example",
            content = "// generated code",
            language = OutputLanguage.KOTLIN,
            processId = "newsletterSubscription",
        )
        every { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") } returns listOf(dummyResource)
        every { bpmnService.extract(any(), any()) } returns dummyModel
        every { codeGenerator.generateCode(any()) } returns listOf(expectedGeneratedFile)
        val command = GenerateProcessApiFromFilesystemUseCase.Command(
            baseDir = "baseDir",
            filePattern = "*.bpmn",
            engine = ProcessEngine.ZEEBE,
            outputFolderPath = "outputFolder",
            outputLanguage = OutputLanguage.KOTLIN,
            packagePath = "de.emaarco.example",
        )

        // when: generateProcessApi is invoked
        underTest.generateProcessApi(command)

        // then: the API code is generated and written to disk
        val expectedModelApi = getExpectedModelApi()
        verify { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") }
        verify { codeGenerator.generateCode(expectedModelApi) }
        verify { fileSystemOutput.writeFiles(listOf(expectedGeneratedFile), "outputFolder") }
        confirmVerified(codeGenerator, bpmnFileLoader, fileSystemOutput)
    }

    private val dummyModel = BpmnModel(
        processId = "newsletterSubscription",
        flowNodes = emptyList(),
        messages = emptyList(),
        signals = emptyList(),
        errors = emptyList(),
    )

    private fun getExpectedModelApi() = testBpmnModelApi(
        model = dummyModel,
        packagePath = "de.emaarco.example",
        language = OutputLanguage.KOTLIN
    )

}
