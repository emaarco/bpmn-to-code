package io.miragon.bpmn.domain.validation.rules

import io.miragon.bpmn.domain.validation.BpmnValidationRule
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationViolation

class MissingSignalNameRule : BpmnValidationRule {

    override val id = "missing-signal-name"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.signals
            .filter { !it.hasName() }
            .map {
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = null,
                    processId = context.model.processId,
                    message = "Signal event definition is missing a 'name' attribute.",
                )
            }
    }
}
