package io.github.emaarco.bpmn.domain.validation

interface BpmnValidationRule {
    val id: String
    val severity: Severity
    val phase: ValidationPhase get() = ValidationPhase.PRE_MERGE
    fun validate(context: ValidationContext): List<ValidationViolation>
}
