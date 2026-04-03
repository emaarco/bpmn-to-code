package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

/** Checks that every BPMN element has an 'id' attribute set. */
class MissingElementIdRule : BpmnValidationRule {

    override val id = "missing-element-id"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val model = context.model
        val violations = mutableListOf<ValidationViolation>()
        checkNullIds(violations, model, model.flowNodes.map { it.id }, "FlowNode")
        checkNullIds(violations, model, model.serviceTasks.map { it.id }, "ServiceTask")
        checkNullIds(violations, model, model.messages.map { it.id }, "Message")
        checkNullIds(violations, model, model.signals.map { it.id }, "Signal")
        checkNullIds(violations, model, model.errors.map { it.id }, "Error")
        checkNullIds(violations, model, model.timers.map { it.id }, "Timer")
        checkNullIds(violations, model, model.callActivities.map { it.id }, "CallActivity")
        return violations
    }

    private fun checkNullIds(
        violations: MutableList<ValidationViolation>,
        model: BpmnModel,
        ids: List<String?>,
        elementType: String,
    ) {
        ids.filter { it == null }.forEach { _ ->
            violations.add(
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = null,
                    processId = model.processId,
                    message = "$elementType has no ID. Every BPMN element must have an 'id' attribute.",
                )
            )
        }
    }
}
