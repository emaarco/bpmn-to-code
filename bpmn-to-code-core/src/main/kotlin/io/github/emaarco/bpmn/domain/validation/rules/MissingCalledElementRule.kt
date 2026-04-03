package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class MissingCalledElementRule : BpmnValidationRule {

    override val id = "missing-called-element"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.callActivities
            .filter { !it.hasCalledElement() }
            .map { callActivity ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = callActivity.id,
                    processId = context.model.processId,
                    message = "Call activity is missing a 'calledElement' or 'processId' attribute.",
                )
            }
    }
}
