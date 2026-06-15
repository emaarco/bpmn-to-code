package io.miragon.bpmn.testing

import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.BpmnValidationRule
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationViolation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CallActivityInputRuleTest {

    private class RequireCallActivityInputsRule(private val required: Set<String>) : BpmnValidationRule {
        override val id = "call-activity-required-inputs"
        override val severity = Severity.ERROR

        override fun validate(context: ValidationContext): List<ValidationViolation> {
            return context.model.callActivities.flatMap { callActivity ->
                val declaredTargets = callActivity.inputMappings.mapNotNull { it.target }.toSet()
                (required - declaredTargets).map { missing ->
                    ValidationViolation(
                        ruleId = id,
                        severity = severity,
                        elementId = callActivity.id,
                        processId = context.model.processId,
                        message = "Call activity '${callActivity.id}' must pass input variable '$missing' to the called process.",
                    )
                }
            }
        }
    }

    private class RequireCallActivityOutputsRule(private val required: Set<String>) : BpmnValidationRule {
        override val id = "call-activity-required-outputs"
        override val severity = Severity.ERROR

        override fun validate(context: ValidationContext): List<ValidationViolation> {
            return context.model.callActivities.flatMap { callActivity ->
                val declaredTargets = callActivity.outputMappings.mapNotNull { it.target }.toSet()
                (required - declaredTargets).map { missing ->
                    ValidationViolation(
                        ruleId = id,
                        severity = severity,
                        elementId = callActivity.id,
                        processId = context.model.processId,
                        message = "Call activity '${callActivity.id}' must return output variable '$missing' to the parent process.",
                    )
                }
            }
        }
    }

    @Test
    fun `custom rule passes when call activity declares the required input targets`() {
        val result = BpmnValidator
            .fromClasspath("bpmn/c7-subscribe-newsletter.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(RequireCallActivityInputsRule(setOf("childSubscriptionId", "childReasonCode")))
            .validate()
            .result()

        assertThat(result.violations).isEmpty()
    }

    @Test
    fun `custom rule flags a call activity missing a required input target`() {
        val result = BpmnValidator
            .fromClasspath("bpmn/c7-subscribe-newsletter.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(RequireCallActivityInputsRule(setOf("businessKey")))
            .validate()
            .result()

        assertThat(result.violations).hasSize(1)
        val violation = result.violations.single()
        assertThat(violation.elementId).isEqualTo("CallActivity_AbortRegistration")
        assertThat(violation.message).contains("businessKey")
    }

    @Test
    fun `custom rule passes when call activity declares the required output target`() {
        val result = BpmnValidator
            .fromClasspath("bpmn/c7-subscribe-newsletter.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(RequireCallActivityOutputsRule(setOf("abortResult")))
            .validate()
            .result()

        assertThat(result.violations).isEmpty()
    }

    @Test
    fun `custom rule flags a call activity missing a required output target`() {
        val result = BpmnValidator
            .fromClasspath("bpmn/c7-subscribe-newsletter.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(RequireCallActivityOutputsRule(setOf("missingResult")))
            .validate()
            .result()

        assertThat(result.violations).hasSize(1)
        val violation = result.violations.single()
        assertThat(violation.elementId).isEqualTo("CallActivity_AbortRegistration")
        assertThat(violation.message).contains("missingResult")
    }
}
