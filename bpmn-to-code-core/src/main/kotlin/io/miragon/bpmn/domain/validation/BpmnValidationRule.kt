package io.miragon.bpmn.domain.validation

import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationPhase
import io.miragon.bpmn.domain.validation.model.ValidationViolation

interface BpmnValidationRule {
    val id: String
    val severity: Severity
    val phase: ValidationPhase get() = ValidationPhase.PRE_MERGE
    fun validate(context: ValidationContext): List<ValidationViolation>
}
