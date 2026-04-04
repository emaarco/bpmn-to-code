package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class EmptyProcessRule : BpmnValidationRule {

    override val id = "empty-process"
    override val severity = Severity.WARN

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        if (context.model.flowNodes.isEmpty()) {
            return listOf(
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = null,
                    processId = context.model.processId,
                    message = "Process has no elements defined.",
                )
            )
        }
        return emptyList()
    }
}
