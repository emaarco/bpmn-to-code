package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import java.io.File

class GenerateProcessApiService(
    private val codeGenerator: GenerateApiCodePort = CodeGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val fileSystemOutput: SaveProcessApiPort = ProcessApiFileSaver(),
) : GenerateProcessApiFromFilesystemUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessApi(command: GenerateProcessApiFromFilesystemUseCase.Command) {
        val validationService = BpmnValidationService(command.validationConfig)
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnService.extract(toBpmnFile(it, command)) }
        validationService.validate(models, command.engine, ValidationPhase.PRE_MERGE)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validate(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        val generatedFiles = mergedModels.map { codeGenerator.generateCode(toBpmnModelApi(it, command)) }
        fileSystemOutput.writeFiles(generatedFiles, command.outputFolderPath)
    }

    private fun toBpmnModelApi(
        model: BpmnModel,
        command: GenerateProcessApiFromFilesystemUseCase.Command,
    ) = BpmnModelApi(
        model = model,
        outputLanguage = command.outputLanguage,
        packagePath = command.packagePath,
        engine = command.engine,
    )

    private fun toBpmnFile(
        bpmnFile: File,
        command: GenerateProcessApiFromFilesystemUseCase.Command
    ) = BpmnResource(
        fileName = bpmnFile.name,
        content = bpmnFile.inputStream(),
        engine = command.engine,
    )

}
