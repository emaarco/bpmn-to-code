package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.validation.ValidationPhase

class GenerateProcessJsonInMemoryService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
) : GenerateProcessJsonInMemoryUseCase {

    override fun generateProcessJson(
        command: GenerateProcessJsonInMemoryUseCase.Command,
    ): List<GeneratedJsonFile> {
        val validationService = BpmnValidationService(command.validationConfig)
        val bpmnResources = command.bpmnContents.map {
            BpmnResource(fileName = it.processName, content = it.bpmnXml.byteInputStream())
        }
        val models = bpmnResources.map { bpmnExtractor.extract(it, command.engine) }
        validationService.validate(models, command.engine, ValidationPhase.PRE_MERGE)
        return models.map { jsonGenerator.generateJson(it) }
    }
}
