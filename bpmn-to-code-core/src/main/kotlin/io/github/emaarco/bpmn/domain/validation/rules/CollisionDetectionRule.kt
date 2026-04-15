package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.service.CollisionDetectionService
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation

class CollisionDetectionRule(
    private val collisionDetectionService: CollisionDetectionService = CollisionDetectionService(),
) : BpmnValidationRule {

    override val id = "collision-detection"
    override val severity = Severity.ERROR
    override val phase = ValidationPhase.POST_MERGE

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val collisions = collisionDetectionService.findCollisions(context.model)
        return collisions.map { detail ->
            val conflicting = detail.conflictingIds.joinToString(", ")
            ValidationViolation(
                ruleId = id,
                severity = severity,
                elementId = null,
                processId = detail.processId,
                message = "[${detail.variableType}] '${detail.constantName}' has conflicting IDs: $conflicting",
            )
        }
    }
}
