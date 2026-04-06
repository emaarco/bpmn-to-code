package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnInMemoryUseCase
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.github.emaarco.bpmn.domain.validation.ValidationViolation
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidateBpmnInMemoryPluginTest {

    private val useCase = mockk<ValidateBpmnInMemoryUseCase>()
    private val underTest = ValidateBpmnInMemoryPlugin(useCase)

    @Test
    fun `execute delegates to use case with correct command`() {

        // given
        val expectedResult = ValidationResult(emptyList())
        every { useCase.validateBpmn(any()) } returns expectedResult

        val firstInput = ValidateBpmnInMemoryPlugin.BpmnInput(bpmnXml = "<bpmn>first</bpmn>", processName = "first.bpmn")
        val secondInput = ValidateBpmnInMemoryPlugin.BpmnInput(bpmnXml = "<bpmn>second</bpmn>", processName = "second.bpmn")

        // when
        val result = underTest.execute(
            bpmnContents = listOf(firstInput, secondInput),
            engine = ProcessEngine.ZEEBE,
        )

        // then
        verify {
            useCase.validateBpmn(
                ValidateBpmnInMemoryUseCase.Command(
                    engine = ProcessEngine.ZEEBE,
                    bpmnContents = listOf(
                        ValidateBpmnInMemoryUseCase.BpmnInput(bpmnXml = "<bpmn>first</bpmn>", processName = "first.bpmn"),
                        ValidateBpmnInMemoryUseCase.BpmnInput(bpmnXml = "<bpmn>second</bpmn>", processName = "second.bpmn"),
                    ),
                )
            )
        }
        assertThat(result).isEqualTo(expectedResult)
        confirmVerified(useCase)
    }

    @Test
    fun `validate convenience method delegates single input`() {

        // given
        val violation = ValidationViolation(
            ruleId = "missing-element-id",
            severity = Severity.ERROR,
            elementId = "task1",
            processId = "myProcess",
            message = "Element has no ID",
        )
        val expectedResult = ValidationResult(listOf(violation))
        every { useCase.validateBpmn(any()) } returns expectedResult

        // when
        val result = underTest.validate(
            bpmnXml = "<bpmn>test</bpmn>",
            engine = ProcessEngine.CAMUNDA_7,
        )

        // then
        verify {
            useCase.validateBpmn(
                ValidateBpmnInMemoryUseCase.Command(
                    engine = ProcessEngine.CAMUNDA_7,
                    bpmnContents = listOf(
                        ValidateBpmnInMemoryUseCase.BpmnInput(bpmnXml = "<bpmn>test</bpmn>", processName = "process"),
                    ),
                )
            )
        }
        assertThat(result).hasSize(1)
        assertThat(result.first().severity).isEqualTo(Severity.ERROR)
        confirmVerified(useCase)
    }
}
