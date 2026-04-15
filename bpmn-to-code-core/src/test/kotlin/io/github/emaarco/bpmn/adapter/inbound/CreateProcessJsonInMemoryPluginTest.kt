package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateProcessJsonInMemoryPluginTest {

    private val useCase = mockk<GenerateProcessJsonInMemoryUseCase>(relaxed = true)
    private val underTest = CreateProcessJsonInMemoryPlugin(useCase)

    @Test
    fun `execute delegates to use case and returns generated files`() {

        // given: multiple BpmnInput objects
        val firstInput = CreateProcessJsonInMemoryPlugin.BpmnInput(bpmnXml = "<bpmn>first</bpmn>", processName = "first.bpmn")
        val secondInput = CreateProcessJsonInMemoryPlugin.BpmnInput(bpmnXml = "<bpmn>second</bpmn>", processName = "second.bpmn")
        val expectedFiles = listOf(
            GeneratedJsonFile(fileName = "first.json", content = "{}"),
            GeneratedJsonFile(fileName = "second.json", content = "{}"),
        )
        every { useCase.generateProcessJson(any()) } returns expectedFiles

        // when: execute is called with multiple inputs
        val result = underTest.execute(
            bpmnContents = listOf(firstInput, secondInput),
            engine = ProcessEngine.ZEEBE,
        )

        // then: use case is called with correct command mapping and returns generated files
        verify {
            useCase.generateProcessJson(
                GenerateProcessJsonInMemoryUseCase.Command(
                    engine = ProcessEngine.ZEEBE,
                    bpmnContents = listOf(
                        GenerateProcessJsonInMemoryUseCase.BpmnInput(bpmnXml = "<bpmn>first</bpmn>", processName = "first.bpmn"),
                        GenerateProcessJsonInMemoryUseCase.BpmnInput(bpmnXml = "<bpmn>second</bpmn>", processName = "second.bpmn"),
                    ),
                )
            )
        }
        assertThat(result).isEqualTo(expectedFiles)
        confirmVerified(useCase)
    }
}
