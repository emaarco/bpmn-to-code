package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation

class MissingTimerDefinitionRule : BpmnValidationRule {

    override val id = "missing-timer-definition"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.timers
            .filter { !it.hasTimerType() }
            .map { timer ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = timer.id,
                    processId = context.model.processId,
                    message = "Timer event definition has no valid type (Date, Duration, or Cycle).",
                )
            }
    }
}
