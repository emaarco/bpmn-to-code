package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.application.ProcessApiGeneration
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.BpmnFileResult

class GenerateProcessApiService(
    private val codeGenerator: GenerateApiCodePort = CodeGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val fileSystemOutput: SaveProcessApiPort = ProcessApiFileSaver(),
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
