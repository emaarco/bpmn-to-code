package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessJsonFileSaver
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.validation.ValidationPhase

class GenerateProcessJsonService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val fileSaver: SaveProcessJsonPort = ProcessJsonFileSaver(),
) : GenerateProcessJsonFromFilesystemUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessJson(command: GenerateProcessJsonFromFilesystemUseCase.Command) {
        val validationService = BpmnValidationService(command.validationConfig)
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnExtractor.extract(it, command.engine) }
        validationService.validate(models, command.engine, ValidationPhase.PRE_MERGE)
        // Run merge for validation only (e.g. missing variant name detection)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validateMerged(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        // JSON is generated per-model (not per-merged-model)
        val generatedFiles = models.map { jsonGenerator.generateJson(it) }
        fileSaver.writeFiles(generatedFiles, command.outputFolderPath)
    }
}
