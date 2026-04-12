package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.ValidationViolation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * End-to-end test that exercises the testing module the way a real user would.
 * Acts as living documentation of the intended user experience.
 */
class BpmnProcessArchitectureTest {

    /**
     * Custom rule: all service tasks must have an ID that starts with a known prefix.
     */
    private val serviceTaskNamingRule = object : BpmnValidationRule {
        override val id = "service-task-naming"
        override val severity = Severity.WARN
        override val phase = ValidationPhase.PRE_MERGE

        override fun validate(context: ValidationContext): List<ValidationViolation> {
            return context.model.serviceTasks
                .filter { task ->
                    val name = task.id ?: ""
                    !name.startsWith("Activity_") && !name.startsWith("Task_")
                }
                .map { task ->
                    ValidationViolation(
                        ruleId = id,
                        severity = severity,
                        elementId = task.id,
                        processId = context.model.processId,
                        message = "Service task '${task.id}' should start with 'Activity_' or 'Task_'",
                    )
                }
        }
    }

    @Test
    fun `validate shared bpmn files with Camunda 7 using built-in and custom rules`() {
        val assert = BpmnValidator
            .fromClasspath("bpmn/c7-subscribe-newsletter.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(
                BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION,
                BpmnRules.MISSING_MESSAGE_NAME,
                BpmnRules.MISSING_ELEMENT_ID,
                serviceTaskNamingRule,
            )
            .validate()

        assert.assertNoErrors()
        assert.assertNoViolations("missing-service-task-implementation")
        assert.assertNoViolations("missing-message-name")
        assert.assertNoViolations("missing-element-id")
    }

    @Test
    fun `validate shared bpmn files with Zeebe`() {
        val assert = BpmnValidator
            .fromClasspath("bpmn/c8-subscribe-newsletter.bpmn")
            .engine(ProcessEngine.ZEEBE)
            .withRules(
                BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION,
                BpmnRules.MISSING_MESSAGE_NAME,
                BpmnRules.MISSING_ELEMENT_ID,
            )
            .validate()

        assert.assertNoErrors()
    }

    @Test
    fun `disableRules filters violations and assertNoViolations confirms`() {
        val assert = BpmnValidator
            .fromClasspath("bpmn/invalid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
            .disableRules("missing-service-task-implementation")
            .validate()

        assert.assertNoViolations("missing-service-task-implementation")
    }

    @Test
    fun `compose built-in and custom rules in single validation`() {
        val allRules = BpmnRules.all() + serviceTaskNamingRule

        val assert = BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(allRules)
            .validate()

        assert.assertNoErrors()
    }

    @Test
    fun `result escape hatch provides raw ValidationResult`() {
        val assert = BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION)
            .validate()

        val result = assert.result()
        assertThat(result.isValid).isTrue()
    }
}
