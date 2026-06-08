package io.github.emaarco.bpmn.domain.validation.model

data class ValidationViolation(
    val ruleId: String,
    val severity: Severity,
    val elementId: String?,
    val processId: String,
    val message: String,
)
