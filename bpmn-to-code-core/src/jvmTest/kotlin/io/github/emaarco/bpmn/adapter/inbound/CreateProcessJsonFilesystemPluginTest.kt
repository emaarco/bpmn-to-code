package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CreateProcessJsonFilesystemPluginTest {

    private val useCase = mockk<GenerateProcessJsonFromFilesystemUseCase>(relaxed = true)
    private val underTest = CreateProcessJsonFilesystemPlugin(useCase)

    @Test
    fun `execute delegates to use case with correct command`() {

        // when: execute is called with all parameters
        underTest.execute(
            baseDir = "/path/to/bpmn",
            filePattern = "*.bpmn",
            outputFolderPath = "/output/folder",
            engine = ProcessEngine.ZEEBE,
        )

        // then: use case is called with correct command mapping
        verify {
            useCase.generateProcessJson(
                GenerateProcessJsonFromFilesystemUseCase.Command(
                    baseDir = "/path/to/bpmn",
                    filePattern = "*.bpmn",
                    outputFolderPath = "/output/folder",
                    engine = ProcessEngine.ZEEBE,
                )
            )
        }
        confirmVerified(useCase)
    }
}
