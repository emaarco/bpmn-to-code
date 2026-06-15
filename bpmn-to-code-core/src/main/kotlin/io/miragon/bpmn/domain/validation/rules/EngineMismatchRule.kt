package io.miragon.bpmn.domain.validation.rules

import io.miragon.bpmn.domain.BpmnModel
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.BpmnValidationRule
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationViolation

/**
 * Flags when a model targets a different engine than the selected one, using the engine detected
 * during extraction ([BpmnModel.detectedEngine]).
 *
 * Zeebe (Camunda 8), Camunda 7, and Operaton each use their own namespace and extension elements,
 * and an extractor only understands its own — so generating for the wrong engine yields a broken API.
 * - a clear mismatch (detected ≠ selected) is an **ERROR**;
 * - an undetermined engine (no recognizable namespace) is a **WARN** — we cannot verify it;
 * - a match produces nothing.
 *
 * Reported like any other rule, so it is collected together with the rest of the findings.
 */
class EngineMismatchRule : BpmnValidationRule {

    override val id = "engine-mismatch"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val detected = (context.model as? BpmnModel)?.detectedEngine
        val selected = context.engine
        val violation = when {
            detected == selected -> null
            detected == null -> violation(context, Severity.WARN, undeterminedMessage(selected))
            else -> violation(context, Severity.ERROR, mismatchMessage(detected, selected))
        }
        return listOfNotNull(violation)
    }

    private fun violation(context: ValidationContext, severity: Severity, message: String): ValidationViolation {
        return ValidationViolation(
            ruleId = id,
            severity = severity,
            elementId = null,
            processId = context.model.processId,
            message = message,
        )
    }

    private fun mismatchMessage(detected: ProcessEngine, selected: ProcessEngine): String {
        val detectedName = displayName(detected)
        val selectedName = displayName(selected)
        return "This model targets $detectedName, but the selected engine is $selectedName. " +
            "These engines use incompatible BPMN configurations — select '$detectedName' " +
            "as the process engine, or provide a model built for $selectedName."
    }

    private fun undeterminedMessage(selected: ProcessEngine): String {
        return "Could not determine this model's target engine from its BPMN namespaces, " +
            "so it cannot be verified against the selected engine (${displayName(selected)}). " +
            "Make sure the model carries the expected engine namespace."
    }

    private fun displayName(engine: ProcessEngine): String = when (engine) {
        ProcessEngine.ZEEBE -> "Zeebe (Camunda 8)"
        ProcessEngine.CAMUNDA_7 -> "Camunda 7"
        ProcessEngine.OPERATON -> "Operaton"
    }
}
