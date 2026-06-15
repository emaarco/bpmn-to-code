package io.miragon.bpmn.application.service

import io.miragon.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.miragon.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.miragon.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.miragon.bpmn.application.port.outbound.ExtractBpmnPort
import io.miragon.bpmn.application.port.outbound.GenerateJsonPort
import io.miragon.bpmn.domain.BpmnResource
import io.miragon.bpmn.domain.GeneratedJsonFile
import io.miragon.bpmn.domain.service.BpmnValidationService
import io.miragon.bpmn.domain.service.ModelMergerService
import io.miragon.bpmn.domain.validation.model.ValidationPhase

class GenerateProcessJsonInMemoryService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
) : GenerateProcessJsonInMemoryUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessJson(
        command: GenerateProcessJsonInMemoryUseCase.Command,
    ): List<GeneratedJsonFile> {
        val validationService = BpmnValidationService(command.validationConfig)
        val bpmnResources = command.bpmnContents.map {
            BpmnResource(fileName = it.processName, content = it.bpmnXml.encodeToByteArray())
        }
        val models = bpmnResources.map { bpmnExtractor.extract(it, command.engine) }
        validationService.validate(models, command.engine, ValidationPhase.PRE_MERGE)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validate(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        return mergedModels.map { jsonGenerator.generateJson(it) }
    }
}
