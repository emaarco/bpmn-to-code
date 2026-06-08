package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.factory.defaultExtractBpmnPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultGenerateApiCodePort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultLoadBpmnFilesPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultSaveProcessApiPort
import io.github.emaarco.bpmn.application.ProcessApiGeneration
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.BpmnFileResult

class GenerateProcessApiService(
    private val codeGenerator: GenerateApiCodePort = defaultGenerateApiCodePort(),
    private val bpmnFileLoader: LoadBpmnFilesPort = defaultLoadBpmnFilesPort(),
    private val bpmnService: ExtractBpmnPort = defaultExtractBpmnPort(),
    private val fileSystemOutput: SaveProcessApiPort = defaultSaveProcessApiPort(),
) : GenerateProcessApiFromFilesystemUseCase {

    override fun generateProcessApi(command: GenerateProcessApiFromFilesystemUseCase.Command): List<BpmnFileResult> {
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnService.extract(it, command.engine) }
        val generatedFiles = ProcessApiGeneration.generate(
            models = models,
            config = ProcessApiGeneration.Config(
                packagePath = command.packagePath,
                outputLanguage = command.outputLanguage,
                engine = command.engine,
                validationConfig = command.validationConfig,
            ),
            codeGenerator = codeGenerator,
        )
        fileSystemOutput.writeFiles(generatedFiles, command.outputFolderPath)
        return inputFiles.zip(models)
            .groupBy({ (_, model) -> model.processId }, { (file, _) -> file.fileName })
            .map { (processId, sourceFiles) -> BpmnFileResult(processId = processId, sourceFiles = sourceFiles) }
    }
}
