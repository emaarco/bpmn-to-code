package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiUseCase
import io.github.emaarco.bpmn.application.port.outbound.ApiVersioningPort
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.WriteApiFilePort
import io.github.emaarco.bpmn.domain.BpmnFile
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.*
import org.junit.jupiter.api.Test
import java.io.File

class GenerateProcessApiServiceTest {

    private val apiFileWriter = mockk<WriteApiFilePort>(relaxed = true)
    private val bpmnFileLoader = mockk<LoadBpmnFilesPort>(relaxed = true)
    private val versionService = mockk<ApiVersioningPort>(relaxed = true)
    private val bpmnService = mockk<ExtractBpmnPort>(relaxed = true)

    private val underTest = GenerateProcessApiService(
        apiFileWriter = apiFileWriter,
        bpmnFileLoader = bpmnFileLoader,
        versionService = versionService,
        bpmnService = bpmnService
    )

    @Test
    fun `generateProcessApi generates API file and increases version`() {

        // given: a dummy BPMN model and a command
        val dummyFile = File("dummy.bpmn")
        every { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") } returns listOf(dummyFile)
        every { bpmnService.extract(BpmnFile(dummyFile, ProcessEngine.ZEEBE)) } returns dummyModel
        every { versionService.getVersion("baseDir", "newsletterSubscription") } returns 0
        val command = GenerateProcessApiUseCase.Command(
            baseDir = "baseDir",
            filePattern = "*.bpmn",
            engine = ProcessEngine.ZEEBE,
            outputFolderPath = "outputFolder",
            outputLanguage = OutputLanguage.KOTLIN,
            packagePath = "de.emaarco.example",
            useVersioning = true
        )

        // when: generateProcessApi is invoked
        underTest.generateProcessApi(command)

        // then: the API file is written and the version is increased
        val expectedModelApi = getExpectedModelApi(apiVersion = 1)
        verify { versionService.getVersion("baseDir", "newsletterSubscription") }
        verify { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") }
        verify { apiFileWriter.writeApiFile(expectedModelApi) }
        verify { versionService.increaseVersion("baseDir", "newsletterSubscription", 1) }
        confirmVerified(apiFileWriter, bpmnFileLoader, versionService)
    }

    @Test
    fun `generateProcessApi generates API file without versioning`() {

        // given: a dummy BPMN model and a command
        val dummyFile = File("dummy.bpmn")
        every { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") } returns listOf(dummyFile)
        every { bpmnService.extract(BpmnFile(dummyFile, ProcessEngine.ZEEBE)) } returns dummyModel
        val command = GenerateProcessApiUseCase.Command(
            baseDir = "baseDir",
            filePattern = "*.bpmn",
            engine = ProcessEngine.ZEEBE,
            outputFolderPath = "outputFolder",
            outputLanguage = OutputLanguage.KOTLIN,
            packagePath = "de.emaarco.example",
            useVersioning = false
        )

        // when: generateProcessApi is invoked
        underTest.generateProcessApi(command)

        // then: the API file is written without versioning
        val expectedModelApi = getExpectedModelApi(apiVersion = null)
        verify { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") }
        verify { apiFileWriter.writeApiFile(expectedModelApi) }
        verify { versionService wasNot called }
        confirmVerified(apiFileWriter, bpmnFileLoader, versionService)
    }

    private val dummyModel = BpmnModel(
        processId = "newsletterSubscription",
        flowNodes = emptyList(),
        serviceTasks = emptyList(),
        messages = emptyList(),
        signals = emptyList(),
        errors = emptyList(),
        timers = emptyList()
    )

    private fun getExpectedModelApi(
        apiVersion: Int? = 1,
    ) = testBpmnModelApi(
        model = dummyModel,
        apiVersion = apiVersion,
        outputFolder = File("outputFolder"),
        packagePath = "de.emaarco.example",
        language = OutputLanguage.KOTLIN
    )

}
