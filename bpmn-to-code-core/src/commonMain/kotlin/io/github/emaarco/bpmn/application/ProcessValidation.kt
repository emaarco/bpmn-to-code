package io.github.emaarco.bpmn.application

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase

/**
 * Synchronous, platform-agnostic core of the validation pipeline (pre-merge -> merge -> post-merge),
 * shared by the JVM validate service and the JS CLI. Stops after pre-merge if it finds an ERROR.
 */
object ProcessValidation {

    fun validate(
        models: List<BpmnModel>,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): ValidationResult {
        val validationService = BpmnValidationService(validationConfig)
        val modelMergerService = ModelMergerService()
        val preMergeViolations = validationService.collectViolations(models, engine, ValidationPhase.PRE_MERGE)
        if (preMergeViolations.any { it.severity == Severity.ERROR }) {
            return ValidationResult(preMergeViolations)
        }
        val mergedModels = modelMergerService.mergeModels(models)
        val postMergeViolations = validationService.collectViolations(mergedModels, engine, ValidationPhase.POST_MERGE)
        return ValidationResult(preMergeViolations + postMergeViolations)
    }
}
