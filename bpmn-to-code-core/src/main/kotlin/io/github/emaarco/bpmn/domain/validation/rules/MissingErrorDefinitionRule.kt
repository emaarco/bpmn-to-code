package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class MissingErrorDefinitionRule : BpmnValidationRule {

    override val id = "missing-error-definition"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.errors
            .filter { !it.hasRequiredFields() }
            .map { error ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = error.id,
                    processId = context.model.processId,
                    message = "Error event definition is missing a 'name' or 'errorCode' attribute.",
                )
            }
    }
}
