package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation

/**
 * Checks that every BPMN element has an 'id' attribute set.
 *
 * All element IDs (service tasks, timers, call activities, errors, signals, messages)
 * are derived from their parent flow node, so checking flow nodes is sufficient.
 */
class MissingElementIdRule : BpmnValidationRule {

    override val id = "missing-element-id"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val model = context.model
        return model.flowNodes
            .filter { it.id == null }
            .map {
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = null,
                    processId = model.processId,
                    message = "FlowNode has no ID. Every BPMN element must have an 'id' attribute.",
                )
            }
    }
}
