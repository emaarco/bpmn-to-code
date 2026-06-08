package io.github.emaarco.bpmn.domain.validation

import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation

interface BpmnValidationRule {
    val id: String
    val severity: Severity
    val phase: ValidationPhase get() = ValidationPhase.PRE_MERGE
    fun validate(context: ValidationContext): List<ValidationViolation>
}
