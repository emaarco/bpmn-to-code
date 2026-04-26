package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation

/**
 * Checks that BPMN element IDs produce valid SCREAMING_SNAKE_CASE identifiers for the generated API.
 */
class InvalidIdentifierRule : BpmnValidationRule {

    override val id = "invalid-identifier"
    override val severity = Severity.WARN

    private val validIdentifier = Regex("^[A-Z_][A-Z0-9_]*$")

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val model = context.model
        val violations = mutableListOf<ValidationViolation>()
        checkMappings(violations, model.processId, model.flowNodes, "FlowNode")
        checkMappings(violations, model.processId, model.serviceTasks, "ServiceTask")
        checkMappings(violations, model.processId, model.messages, "Message")
        checkMappings(violations, model.processId, model.signals, "Signal")
        checkMappings(violations, model.processId, model.errors, "Error")
        checkMappings(violations, model.processId, model.escalations, "Escalation")
        checkMappings(violations, model.processId, model.compensations, "Compensation")
        checkMappings(violations, model.processId, model.callActivities, "CallActivity")
        checkMappings(violations, model.processId, model.timers, "Timer")
        checkMappings(violations, model.processId, model.variables, "Variable")
        return violations
    }

    private fun <T : Any> checkMappings(
        violations: MutableList<ValidationViolation>,
        processId: String,
        items: List<VariableMapping<T>>,
        elementType: String,
    ) {
        items.filter { it.getRawName().isNotEmpty() && it.getName().isNotEmpty() }
            .filter { !validIdentifier.matches(it.getName()) }
            .forEach {
                violations.add(
                    ValidationViolation(
                        ruleId = id,
                        severity = severity,
                        elementId = it.getRawName(),
                        processId = processId,
                        message = "$elementType '${it.getRawName()}' produces invalid identifier '${it.getName()}'.",
                    )
                )
            }
    }
}
