package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.factory.defaultExtractBpmnPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultLoadBpmnFilesPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultSaveProcessJsonPort
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase

class GenerateProcessJsonService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = defaultLoadBpmnFilesPort(),
    private val bpmnExtractor: ExtractBpmnPort = defaultExtractBpmnPort(),
    private val fileSaver: SaveProcessJsonPort = defaultSaveProcessJsonPort(),
) : GenerateProcessJsonFromFilesystemUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessJson(command: GenerateProcessJsonFromFilesystemUseCase.Command) {
        val validationService = BpmnValidationService(command.validationConfig)
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnExtractor.extract(it, command.engine) }
        validationService.validate(models, command.engine, ValidationPhase.PRE_MERGE)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validate(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        val generatedFiles = mergedModels.map { jsonGenerator.generateJson(it) }
        fileSaver.writeFiles(generatedFiles, command.outputFolderPath)
    }
}
