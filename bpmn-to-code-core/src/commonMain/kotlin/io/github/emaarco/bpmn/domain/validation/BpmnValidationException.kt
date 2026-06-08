package io.github.emaarco.bpmn.domain.validation

import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation

class BpmnValidationException(
    val violations: List<ValidationViolation>,
) : RuntimeException(buildMessage(violations)) {

    companion object {
        private fun buildMessage(violations: List<ValidationViolation>): String {
            val errorCount = violations.count { it.severity == Severity.ERROR }
            val warnCount = violations.count { it.severity == Severity.WARN }
            val header = "BPMN validation failed: $errorCount error(s), $warnCount warning(s)\n"
            val details = violations.joinToString("\n") { violation ->
                val level = if (violation.severity == Severity.ERROR) "ERROR" else "WARN"
                val location = if (violation.elementId != null) {
                    "${violation.processId}/${violation.elementId}"
                } else {
                    violation.processId
                }
                "[BPMN VALIDATION $level] $location: ${violation.message} (rule: ${violation.ruleId})"
            }
            return header + details
        }
    }
}
