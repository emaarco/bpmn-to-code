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
        checkMappings(model.callActivities, "CallActivity")

        return violations
    }
}
