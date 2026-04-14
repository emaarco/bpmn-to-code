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

    private val underTest = BpmnValidationService()

    @Test
    fun `valid model passes all pre-merge rules`() {

        // given: a valid BPMN model
        val model = testBpmnModel()

        // when / then: no exception is thrown
        assertDoesNotThrow { underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE) }
    }

    @Test
    fun `throws BpmnValidationException for missing service task implementation`() {

        // given: a model with a service task that has no implementation
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")),
                )
            )
        )

        // when: validating pre-merge
        val exception = assertThrows<BpmnValidationException> {
            underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }

        // then: the missing-implementation rule fires
        assertThat(exception.violations).anyMatch { it.ruleId == "missing-service-task-implementation" }
    }

    @Test
    fun `disabled rule is skipped during validation`() {

        // given: a service with the implementation rule disabled and a model that would violate it
        val underTest = BpmnValidationService(
            ValidationConfig(disabledRules = setOf("missing-service-task-implementation"))
        )
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")),
                )
            )
        )

        // when / then: no exception is thrown because the rule is disabled
        assertDoesNotThrow { underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE) }
    }

    @Test
    fun `warnings do not throw by default`() {

        // given: a model that produces only warnings (empty process)
        val model = testBpmnModel(flowNodes = emptyList())

        // when / then: no exception is thrown
        assertDoesNotThrow { underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE) }
    }

    @Test
    fun `failOnWarning promotes warnings to failures`() {

        // given: a service with failOnWarning and a model with an empty process
        val underTest = BpmnValidationService(ValidationConfig(failOnWarning = true))
        val model = testBpmnModel(flowNodes = emptyList())

        // when: validating pre-merge
        val exception = assertThrows<BpmnValidationException> {
            underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }

        // then: the empty-process warning is treated as a failure
        assertThat(exception.violations).anyMatch { it.ruleId == "empty-process" }
    }

    @Test
    fun `throws BpmnValidationException for flow node with null element id`() {

        // given: a model containing a flow node without an ID
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = null))
        )

        // when: validating pre-merge
        val exception = assertThrows<BpmnValidationException> {
            underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.PRE_MERGE)
        }

        // then: the missing-element-id rule fires with ERROR severity
        assertThat(exception.violations).anyMatch {
            it.ruleId == "missing-element-id" && it.severity == Severity.ERROR
        }
    }

    @Test
    fun `post-merge collision detection detects collisions`() {

        // given: a model with two flow nodes that produce the same constant name
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "endEvent_complete"),
                FlowNodeDefinition(id = "endEvent-complete"),
            )
        )

        // when: validating post-merge
        val exception = assertThrows<BpmnValidationException> {
            underTest.validate(listOf(model), ProcessEngine.ZEEBE, ValidationPhase.POST_MERGE)
        }

        // then: the collision-detection rule fires
        assertThat(exception.violations).anyMatch { it.ruleId == "collision-detection" }
    }
}
