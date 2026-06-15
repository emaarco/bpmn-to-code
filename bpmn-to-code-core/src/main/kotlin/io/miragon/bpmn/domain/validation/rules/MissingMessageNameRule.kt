package io.miragon.bpmn.domain.validation.rules

import io.miragon.bpmn.domain.validation.BpmnValidationRule
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationViolation

class MissingMessageNameRule : BpmnValidationRule {

    override val id = "missing-message-name"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.messages
            .filter { !it.hasName() }
            .map { message ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = message.id,
                    processId = context.model.processId,
                    message = "Message element is missing a 'name' attribute.",
                )
            }
    }
}
