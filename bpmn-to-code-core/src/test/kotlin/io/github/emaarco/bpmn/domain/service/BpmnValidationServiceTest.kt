package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class BpmnValidationServiceTest {

    @Test
    fun `valid model passes all pre-merge rules`() {
        val service = BpmnValidationService()
        val model = testBpmnModel()
        assertDoesNotThrow {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }
    }

    @Test
    fun `throws BpmnValidationException for missing implementation`() {
        val service = BpmnValidationService()
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "task1", properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")))
            )
        )
        val exception = assertThrows<BpmnValidationException> {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }
        assertThat(exception.violations).anyMatch { it.ruleId == "missing-service-task-implementation" }
    }

    @Test
    fun `disabled rule is skipped`() {
        val config = ValidationConfig(disabledRules = setOf("missing-service-task-implementation"))
        val service = BpmnValidationService(config)
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "task1", properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")))
            )
        )
        assertDoesNotThrow {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }
    }

    @Test
    fun `warnings do not throw by default`() {
        val service = BpmnValidationService()
        val model = testBpmnModel(flowNodes = emptyList())
        assertDoesNotThrow {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }
    }

    @Test
    fun `failOnWarning promotes warnings to failures`() {
        val config = ValidationConfig(failOnWarning = true)
        val service = BpmnValidationService(config)
        val model = testBpmnModel(flowNodes = emptyList())
        val exception = assertThrows<BpmnValidationException> {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }
        assertThat(exception.violations).anyMatch { it.ruleId == "empty-process" }
    }

    @Test
    fun `throws BpmnValidationException for null element id`() {
        val service = BpmnValidationService()
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = null))
        )
        val exception = assertThrows<BpmnValidationException> {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }
        assertThat(exception.violations).anyMatch {
            it.ruleId == "missing-element-id" && it.severity == Severity.ERROR
        }
    }

    @Test
    fun `post-merge collision detection detects collisions`() {
        val service = BpmnValidationService()
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "endEvent_complete"),
                FlowNodeDefinition(id = "endEvent-complete"),
            )
        )
        val exception = assertThrows<BpmnValidationException> {
            service.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.POST_MERGE)
        }
        assertThat(exception.violations).anyMatch { it.ruleId == "collision-detection" }
    }
}
