package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class MissingImplementationRule : BpmnValidationRule {

    override val id = "missing-implementation"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        return context.model.serviceTasks
            .filter { !it.hasImplementation() }
            .map { task ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = task.id,
                    processId = context.model.processId,
                    message = "Service task has no implementation. ${engineHint(context.engine)}",
                )
            }
    }

    private fun engineHint(engine: ProcessEngine): String = when (engine) {
        ProcessEngine.CAMUNDA_7 -> "Set camunda:topic, camunda:class, or camunda:delegateExpression."
        ProcessEngine.OPERATON -> "Set operaton:topic, operaton:class, or operaton:delegateExpression."
        ProcessEngine.ZEEBE -> "Add a zeebe:taskDefinition with a type attribute."
    }
}
