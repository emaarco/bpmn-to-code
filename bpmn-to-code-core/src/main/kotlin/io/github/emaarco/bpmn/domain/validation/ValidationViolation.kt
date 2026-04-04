package io.github.emaarco.bpmn.domain.validation

data class ValidationViolation(
    val ruleId: String,
    val severity: Severity,
    val elementId: String?,
    val processId: String,
    val message: String,
)
