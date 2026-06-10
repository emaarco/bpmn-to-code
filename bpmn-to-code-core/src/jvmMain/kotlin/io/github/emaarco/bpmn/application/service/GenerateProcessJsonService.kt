package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessJsonFileSaver
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.ProcessJsonGeneration
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort

class GenerateProcessJsonService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val fileSaver: SaveProcessJsonPort = ProcessJsonFileSaver(),
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
