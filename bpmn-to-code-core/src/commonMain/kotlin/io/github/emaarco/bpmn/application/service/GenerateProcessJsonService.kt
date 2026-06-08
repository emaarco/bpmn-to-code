package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.factory.defaultExtractBpmnPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultLoadBpmnFilesPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultSaveProcessJsonPort
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.ProcessJsonGeneration
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort

class GenerateProcessJsonService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = defaultLoadBpmnFilesPort(),
    private val bpmnExtractor: ExtractBpmnPort = defaultExtractBpmnPort(),
    private val fileSaver: SaveProcessJsonPort = defaultSaveProcessJsonPort(),
) : GenerateProcessJsonFromFilesystemUseCase {

    override fun generateProcessJson(command: GenerateProcessJsonFromFilesystemUseCase.Command) {
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnExtractor.extract(it, command.engine) }
        val generatedFiles = ProcessJsonGeneration.generate(
            models = models,
            engine = command.engine,
            validationConfig = command.validationConfig,
            jsonGenerator = jsonGenerator,
        )
        fileSaver.writeFiles(generatedFiles, command.outputFolderPath)
    }
}
