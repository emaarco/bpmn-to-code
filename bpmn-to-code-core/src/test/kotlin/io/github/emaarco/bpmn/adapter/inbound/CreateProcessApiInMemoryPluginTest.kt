package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.adapter.logger.NoOpLoggerAdapter
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateProcessApiInMemoryPluginTest {

    private val logger = NoOpLoggerAdapter()
    private val useCase = mockk<GenerateProcessApiInMemoryUseCase>(relaxed = true)
    private val underTest = CreateProcessApiInMemoryPlugin(logger, useCase)

    @Test
    fun `execute delegates to use case and returns generated files`() {

        // given: multiple BpmnInput objects
        val firstInput = mockInput("first")
        val secondInput = mockInput("second")
        val expectedFiles = listOf(mockApiFile("first.kt"), mockApiFile("second.kt"))
        every { useCase.generateProcessApi(any()) } returns expectedFiles

        // when: execute is called with multiple inputs
        val result = underTest.execute(
            bpmnContents = listOf(firstInput, secondInput),
            packagePath = "com.example.api",
            outputLanguage = OutputLanguage.KOTLIN,
            engine = ProcessEngine.ZEEBE
        )

        // then: a use case is called with correct command mapping and returns generated files
        verify {
            useCase.generateProcessApi(
                GenerateProcessApiInMemoryUseCase.Command(
                    packagePath = "com.example.api",
                    outputLanguage = OutputLanguage.KOTLIN,
                    engine = ProcessEngine.ZEEBE,
                    bpmnContents = listOf(
                        GenerateProcessApiInMemoryUseCase.BpmnInput(
                            bpmnXml = "<bpmn>first</bpmn>",
                            processName = "first.bpmn"
                        ),
                        GenerateProcessApiInMemoryUseCase.BpmnInput(
                            bpmnXml = "<bpmn>second</bpmn>",
                            processName = "second.bpmn"
                        )
                    )
                )
            )
        }

        assertThat(result).isEqualTo(expectedFiles)
        assertThat(result).hasSize(2)
        confirmVerified(useCase)
    }

    private fun mockInput(fileName: String) = CreateProcessApiInMemoryPlugin.BpmnInput(
        bpmnXml = "<bpmn>$fileName</bpmn>",
        processName = "$fileName.bpmn"
    )

    private fun mockApiFile(fileName: String) = GeneratedApiFile(
        fileName = fileName,
        packagePath = "com.example.api",
        content = "// $fileName api code",
        language = OutputLanguage.KOTLIN
    )

}
