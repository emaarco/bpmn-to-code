package io.github.emaarco.bpmn.application

import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase

/**
 * Synchronous, platform-agnostic core of the JSON-descriptor pipeline (validate -> merge -> generate)
 * over already-parsed models. Shared by the JVM JSON services and the JS CLI so merge semantics match.
 */
object ProcessJsonGeneration {

    fun generate(
        models: List<BpmnModel>,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
        jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    ): List<GeneratedJsonFile> {
        val validationService = BpmnValidationService(validationConfig)
        val modelMergerService = ModelMergerService()
        validationService.validate(models, engine, ValidationPhase.PRE_MERGE)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validate(mergedModels, engine, ValidationPhase.POST_MERGE)
        return mergedModels.map { jsonGenerator.generateJson(it) }
    }
}
