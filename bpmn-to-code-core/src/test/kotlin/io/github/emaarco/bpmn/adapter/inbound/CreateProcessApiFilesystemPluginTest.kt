package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CreateProcessApiFilesystemPluginTest {

    private val useCase = mockk<GenerateProcessApiFromFilesystemUseCase>(relaxed = true)
    private val underTest = CreateProcessApiFilesystemPlugin(useCase)

    @Test
    fun `execute delegates to use case with correct command`() {

        // when: execute is called with all parameters
        underTest.execute(
            baseDir = "/path/to/bpmn",
            filePattern = "*.bpmn",
            outputFolderPath = "/output/folder",
            packagePath = "com.example.api",
            outputLanguage = OutputLanguage.KOTLIN,
            engine = ProcessEngine.ZEEBE,
            useVersioning = true
        )

        // then: use case is called with correct command mapping
        verify {
            useCase.generateProcessApi(
                GenerateProcessApiFromFilesystemUseCase.Command(
                    baseDir = "/path/to/bpmn",
                    filePattern = "*.bpmn",
                    outputFolderPath = "/output/folder",
                    packagePath = "com.example.api",
                    outputLanguage = OutputLanguage.KOTLIN,
                    engine = ProcessEngine.ZEEBE,
                    useVersioning = true
                )
            )
        }
        confirmVerified(useCase)
    }
}
