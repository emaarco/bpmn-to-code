package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class InvalidIdentifierRule : BpmnValidationRule {

    override val id = "invalid-identifier"
    override val severity = Severity.WARN

    private val validIdentifier = Regex("^[A-Z_][A-Z0-9_]*$")

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val model = context.model
        val violations = mutableListOf<ValidationViolation>()

        fun checkNullId(elementId: String?, elementType: String) {
            if (elementId == null) {
                violations.add(
                    ValidationViolation(
                        ruleId = id,
                        severity = Severity.ERROR,
                        elementId = null,
                        processId = model.processId,
                        message = "$elementType has no ID. Every BPMN element must have an 'id' attribute.",
                    )
                )
            }
        }

        model.flowNodes.forEach { checkNullId(it.id, "FlowNode") }
        model.serviceTasks.forEach { checkNullId(it.id, "ServiceTask") }
        model.messages.forEach { checkNullId(it.id, "Message") }
        model.signals.forEach { checkNullId(it.id, "Signal") }
        model.errors.forEach { checkNullId(it.id, "Error") }
        model.timers.forEach { checkNullId(it.id, "Timer") }
        model.callActivities.forEach { checkNullId(it.id, "CallActivity") }

        fun <T : Any> checkMappings(items: List<VariableMapping<T>>, elementType: String) {
            items.filter { it.getRawName().isNotEmpty() && it.getName().isNotEmpty() }
                .filter { !validIdentifier.matches(it.getName()) }
                .forEach {
                    violations.add(
                        ValidationViolation(
                            ruleId = id,
                            severity = severity,
                            elementId = it.getRawName(),
                            processId = model.processId,
                            message = "$elementType '${it.getRawName()}' produces invalid identifier '${it.getName()}'.",
                        )
                    )
                }
        }

        checkMappings(model.flowNodes, "FlowNode")
        checkMappings(model.serviceTasks, "ServiceTask")
        checkMappings(model.messages, "Message")
        checkMappings(model.signals, "Signal")
        checkMappings(model.errors, "Error")
        checkMappings(model.timers, "Timer")
        checkMappings(model.callActivities, "CallActivity")

        return violations
    }
}
