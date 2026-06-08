package io.github.emaarco.bpmn.application

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.ProcessModel
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase

/**
 * Synchronous, platform-agnostic core of the generation pipeline (validate -> merge -> generate) over
 * already-parsed models. Lives outside `application.service` to satisfy the konsist rule that every
 * file there is a `*Service` importing one inbound port.
 */
object ProcessApiGeneration {

    fun generate(
        models: List<BpmnModel>,
        config: Config,
        codeGenerator: GenerateApiCodePort = CodeGenerationAdapter(),
    ): List<GeneratedApiFile> {
        val validationService = BpmnValidationService(config.validationConfig)
        val modelMergerService = ModelMergerService()
        validationService.validate(models, config.engine, ValidationPhase.PRE_MERGE)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validate(mergedModels, config.engine, ValidationPhase.POST_MERGE)
        return mergedModels
            .flatMap { codeGenerator.generateCode(toModelApi(config, it)) }
            .distinctBy { it.packagePath to it.fileName }
    }

    private fun toModelApi(config: Config, model: ProcessModel): BpmnModelApi {
        return BpmnModelApi(
            model = model,
            outputLanguage = config.outputLanguage,
            packagePath = config.packagePath,
            engine = config.engine,
        )
    }

    data class Config(
        val packagePath: String,
        val outputLanguage: OutputLanguage,
        val engine: ProcessEngine,
        val validationConfig: ValidationConfig = ValidationConfig(),
    )
}
