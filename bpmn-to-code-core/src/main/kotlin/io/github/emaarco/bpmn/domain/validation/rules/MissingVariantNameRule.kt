package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class MissingVariantNameRule : BpmnValidationRule {

    override val id = "missing-variant-name"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val model = context.model
        val allModels = context.allModels
        val modelsWithSameProcessId = allModels.filter { it.processId == model.processId }
        if (modelsWithSameProcessId.size <= 1) return emptyList()

        // Only report from the first model to avoid duplicate violations
        if (modelsWithSameProcessId.first() !== model) return emptyList()

        val modelsWithoutVariant = modelsWithSameProcessId.filter { it.variantName.isNullOrBlank() }
        if (modelsWithoutVariant.isEmpty()) return emptyList()

        return listOf(
            ValidationViolation(
                ruleId = id,
                severity = severity,
                elementId = null,
                processId = model.processId,
                message = "Multiple BPMN files share process ID '${model.processId}' but not all define a variantName. " +
                    "Add a variantName extension property to each process.",
            )
        )
    }
}
