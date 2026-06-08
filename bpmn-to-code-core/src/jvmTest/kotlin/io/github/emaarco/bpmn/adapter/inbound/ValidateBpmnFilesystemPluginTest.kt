package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidateBpmnFilesystemPluginTest {

    private val useCase = mockk<ValidateBpmnFromFilesystemUseCase>(relaxed = true)
    private val underTest = ValidateBpmnFilesystemPlugin(useCase = useCase)

    @Test
    fun `execute delegates to use case with correct command`() {

        // given: a config and an expected validation result
        val config = ValidationConfig(failOnWarning = true, disabledRules = setOf("missing-element-id"))
        val expectedResult = ValidationResult(emptyList())
        every { useCase.validateBpmn(any()) } returns expectedResult

        // when: execute is called with all parameters
        val result = underTest.execute(
            baseDir = "/path/to/bpmn",
            filePattern = "*.bpmn",
            engine = ProcessEngine.ZEEBE,
            validationConfig = config,
        )

        // then: use case is called with the correct command and result is returned
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

    @Test
    fun `execute uses default validation config when not provided`() {

        // given: an expected validation result
        val expectedResult = ValidationResult(emptyList())
        every { useCase.validateBpmn(any()) } returns expectedResult

        // when: execute is called without explicit validationConfig
        val result = underTest.execute(
            baseDir = "/path/to/bpmn",
            filePattern = "*.bpmn",
            engine = ProcessEngine.CAMUNDA_7,
        )

        // then: use case is called with a default validation config
        verify {
            useCase.validateBpmn(
                ValidateBpmnFromFilesystemUseCase.Command(
                    baseDir = "/path/to/bpmn",
                    filePattern = "*.bpmn",
                    engine = ProcessEngine.CAMUNDA_7,
                    validationConfig = ValidationConfig(),
                )
            )
        }
        assertThat(result).isEqualTo(expectedResult)
        confirmVerified(useCase)
    }
}
