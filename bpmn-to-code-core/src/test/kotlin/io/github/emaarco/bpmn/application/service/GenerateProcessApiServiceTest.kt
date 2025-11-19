package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.logger.NoOpLoggerAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ApiVersioningPort
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.File

class GenerateProcessApiServiceTest {

    private val logger = NoOpLoggerAdapter()
    private val codeGenerator = mockk<GenerateApiCodePort>(relaxed = true)
    private val bpmnFileLoader = mockk<LoadBpmnFilesPort>(relaxed = true)
    private val versionService = mockk<ApiVersioningPort>(relaxed = true)
    private val bpmnService = mockk<ExtractBpmnPort>(relaxed = true)
    private val fileSystemOutput = mockk<ProcessApiFileSaver>(relaxed = true)

    private val underTest = GenerateProcessApiService(
        logger = logger,
        codeGenerator = codeGenerator,
        bpmnFileLoader = bpmnFileLoader,
        versionService = versionService,
        bpmnService = bpmnService,
        fileSystemOutput = fileSystemOutput
    )

    @Test
    fun `generateProcessApi generates API file and increases version`() {

        // given: a dummy BPMN model and a command
        val dummyFile = File.createTempFile("dummy", ".bpmn").apply {
            writeText("<bpmn></bpmn>")
            deleteOnExit()
        }
        val expectedGeneratedFile = GeneratedApiFile(
            fileName = "NewsletterSubscriptionProcessApiV1.kt",
            packagePath = "de.emaarco.example",
            content = "// generated code",
            language = OutputLanguage.KOTLIN
        )
        every { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") } returns listOf(dummyFile)
        every { bpmnService.extract(any()) } returns dummyModel
        every { versionService.getVersion("baseDir", "newsletterSubscription") } returns 0
        every { codeGenerator.generateCode(any()) } returns expectedGeneratedFile
        val command = GenerateProcessApiFromFilesystemUseCase.Command(
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

        // then: the API code is generated, written to disk, and the version is increased
        val expectedModelApi = getExpectedModelApi(apiVersion = 1)
        verify { versionService.getVersion("baseDir", "newsletterSubscription") }
        verify { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") }
        verify { codeGenerator.generateCode(expectedModelApi) }
        verify { versionService.increaseVersion("baseDir", "newsletterSubscription", 1) }
        verify { fileSystemOutput.writeFiles(listOf(expectedGeneratedFile), "outputFolder") }
        confirmVerified(codeGenerator, bpmnFileLoader, versionService, fileSystemOutput)
    }

    @Test
    fun `generateProcessApi generates API file without versioning`() {

        // given: a dummy BPMN model and a command
        val dummyFile = File.createTempFile("dummy", ".bpmn").apply {
            writeText("<bpmn></bpmn>")
            deleteOnExit()
        }
        val expectedGeneratedFile = GeneratedApiFile(
            fileName = "NewsletterSubscriptionProcessApi.kt",
            packagePath = "de.emaarco.example",
            content = "// generated code",
            language = OutputLanguage.KOTLIN
        )
        every { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") } returns listOf(dummyFile)
        every { bpmnService.extract(any()) } returns dummyModel
        every { codeGenerator.generateCode(any()) } returns expectedGeneratedFile
        val command = GenerateProcessApiFromFilesystemUseCase.Command(
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

        // then: the API code is generated and written to disk without versioning
        val expectedModelApi = getExpectedModelApi(apiVersion = null)
        verify { bpmnFileLoader.loadFrom("baseDir", "*.bpmn") }
        verify { codeGenerator.generateCode(expectedModelApi) }
        verify { fileSystemOutput.writeFiles(listOf(expectedGeneratedFile), "outputFolder") }
        verify { versionService wasNot called }
        confirmVerified(codeGenerator, bpmnFileLoader, versionService, fileSystemOutput)
    }

    private val dummyModel = BpmnModel(
        processId = "newsletterSubscription",
        flowNodes = emptyList(),
        serviceTasks = emptyList(),
        messages = emptyList(),
        signals = emptyList(),
        errors = emptyList(),
        timers = emptyList(),
        variables = emptyList()
    )

    private fun getExpectedModelApi(
        apiVersion: Int? = 1,
    ) = testBpmnModelApi(
        model = dummyModel,
        apiVersion = apiVersion,
        packagePath = "de.emaarco.example",
        language = OutputLanguage.KOTLIN
    )

}
