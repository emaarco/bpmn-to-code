package io.github.emaarco.bpmn.domain.validation

data class ValidationResult(
    val violations: List<ValidationViolation>,
) {
    val errors: List<ValidationViolation> get() = violations.filter { it.severity == Severity.ERROR }
    val warnings: List<ValidationViolation> get() = violations.filter { it.severity == Severity.WARN }
    val hasErrors: Boolean get() = errors.isNotEmpty()
    val isValid: Boolean get() = violations.isEmpty()
    fun hasFailures(failOnWarning: Boolean): Boolean = hasErrors || (failOnWarning && warnings.isNotEmpty())
}
