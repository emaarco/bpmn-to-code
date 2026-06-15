package io.miragon.bpmn.domain.service

import io.miragon.bpmn.domain.ProcessModel
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.BpmnValidationException
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationConfig
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationPhase
import io.miragon.bpmn.domain.validation.model.ValidationViolation
import io.miragon.bpmn.domain.validation.rules.CollisionDetectionRule
import io.miragon.bpmn.domain.validation.rules.EmptyProcessRule
import io.miragon.bpmn.domain.validation.rules.EngineMismatchRule
import io.miragon.bpmn.domain.validation.rules.InvalidIdentifierRule
import io.miragon.bpmn.domain.validation.rules.MissingCalledElementRule
import io.miragon.bpmn.domain.validation.rules.MissingElementIdRule
import io.miragon.bpmn.domain.validation.rules.MissingErrorDefinitionRule
import io.miragon.bpmn.domain.validation.rules.MissingServiceTaskImplementationRule
import io.miragon.bpmn.domain.validation.rules.MissingMessageNameRule
import io.miragon.bpmn.domain.validation.rules.MissingProcessIdRule
import io.miragon.bpmn.domain.validation.rules.MissingSignalNameRule
import io.miragon.bpmn.domain.validation.rules.MissingTimerDefinitionRule
import io.github.oshai.kotlinlogging.KotlinLogging

class BpmnValidationService(
    private val config: ValidationConfig = ValidationConfig(),
) {

    private val logger = KotlinLogging.logger {}

    private val allRules = builtInRules()

    fun collectViolations(models: List<ProcessModel>, engine: ProcessEngine, phase: ValidationPhase): List<ValidationViolation> {
        val activeRules = allRules
            .filter { it.phase == phase }
            .filterNot { it.id in config.disabledRules }

        return models.flatMap { model ->
            val ctx = ValidationContext(model, engine)
            activeRules.flatMap { it.validate(ctx) }
        }
    }

    fun validate(models: List<ProcessModel>, engine: ProcessEngine, phase: ValidationPhase) {
        val violations = collectViolations(models, engine, phase)
        logViolations(violations)
        throwIfNeeded(violations)
    }

    private fun logViolations(violations: List<ValidationViolation>) {
        violations.filter { it.severity == Severity.WARN }.forEach { violation ->
            val location = if (violation.elementId != null) {
                "${violation.processId}/${violation.elementId}"
            } else {
                violation.processId
            }
            logger.warn { "[BPMN VALIDATION WARN] $location: ${violation.message} (rule: ${violation.ruleId})" }
        }
    }

    private fun throwIfNeeded(violations: List<ValidationViolation>) {
        val errors = violations.filter { it.severity == Severity.ERROR }
        val warnings = violations.filter { it.severity == Severity.WARN }
        val failingViolations = errors + if (config.failOnWarning) warnings else emptyList()
        if (failingViolations.isNotEmpty()) {
            throw BpmnValidationException(failingViolations)
        }
    }

    companion object {

        private fun builtInRules() = listOf(
            EngineMismatchRule(),
            MissingServiceTaskImplementationRule(),
            MissingMessageNameRule(),
            MissingErrorDefinitionRule(),
            MissingSignalNameRule(),
            MissingTimerDefinitionRule(),
            MissingCalledElementRule(),
            MissingElementIdRule(),
            InvalidIdentifierRule(),
            EmptyProcessRule(),
            MissingProcessIdRule(),
            CollisionDetectionRule(),
        )
    }
}
