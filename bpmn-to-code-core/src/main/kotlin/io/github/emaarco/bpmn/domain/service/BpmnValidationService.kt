package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.ProcessModel
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation
import io.github.emaarco.bpmn.domain.validation.rules.CollisionDetectionRule
import io.github.emaarco.bpmn.domain.validation.rules.EmptyProcessRule
import io.github.emaarco.bpmn.domain.validation.rules.InvalidIdentifierRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingCalledElementRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingElementIdRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingErrorDefinitionRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingServiceTaskImplementationRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingMessageNameRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingProcessIdRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingSignalNameRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingTimerDefinitionRule
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
