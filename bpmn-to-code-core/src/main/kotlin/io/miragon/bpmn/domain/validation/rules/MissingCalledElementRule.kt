package io.miragon.bpmn.domain.validation.rules

import io.miragon.bpmn.domain.validation.BpmnValidationRule
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationViolation

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
