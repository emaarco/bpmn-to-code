package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidateBpmnFilesystemPluginTest {

    private val useCase = mockk<ValidateBpmnFromFilesystemUseCase>()
    private val underTest = ValidateBpmnFilesystemPlugin(useCase)

    @Test
    fun `execute delegates to use case with correct command`() {

        // given
        val expectedResult = ValidationResult(emptyList())
        every { useCase.validateBpmn(any()) } returns expectedResult

        // when
        val config = ValidationConfig(failOnWarning = true, disabledRules = setOf("missing-element-id"))
        val result = underTest.execute(
            baseDir = "/path/to/bpmn",
            filePattern = "*.bpmn",
            engine = ProcessEngine.ZEEBE,
            validationConfig = config,
        )

        // then
        verify {
            useCase.validateBpmn(
                ValidateBpmnFromFilesystemUseCase.Command(
                    baseDir = "/path/to/bpmn",
                    filePattern = "*.bpmn",
                    engine = ProcessEngine.ZEEBE,
                    validationConfig = config,
                )
            )
        }
        assertThat(result).isEqualTo(expectedResult)
        confirmVerified(useCase)
    }
}
