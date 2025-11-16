package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.adapter.outbound.versioning.VersionService
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ApiVersioningPort
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import java.io.File

class GenerateProcessApiService(
    private val codeGenerator: GenerateApiCodePort = CodeGenerationAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val versionService: ApiVersioningPort = VersionService(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val fileSystemOutput: SaveProcessApiPort = ProcessApiFileSaver(),
) : GenerateProcessApiFromFilesystemUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessApi(command: GenerateProcessApiFromFilesystemUseCase.Command) {
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnService.extract(toBpmnFile(it, command)) }
        val mergedModels = modelMergerService.mergeModels(models)
        val generatedFiles = this.generateApiFiles(mergedModels, command)
        fileSystemOutput.writeFiles(generatedFiles, command.outputFolderPath)
    }

    private fun generateApiFiles(
        models: List<BpmnModel>,
        command: GenerateProcessApiFromFilesystemUseCase.Command,
    ) = if (command.useVersioning) {
        this.createVersionedProcessApis(models, command)
    } else {
        this.createUnversionedProcessApis(models, command)
    }

    private fun createVersionedProcessApis(
        models: List<BpmnModel>,
        command: GenerateProcessApiFromFilesystemUseCase.Command
    ): List<GeneratedApiFile> = models.map {
        val currentVersion = this.versionService.getVersion(command.baseDir, it.processId)
        val nextVersion = currentVersion + 1
        val modelApi = toBpmnModelApi(it, command, nextVersion)
        val generatedFile = this.codeGenerator.generateCode(modelApi)
        this.versionService.increaseVersion(command.baseDir, it.processId, nextVersion)
        generatedFile
    }

    private fun createUnversionedProcessApis(
        models: List<BpmnModel>,
        command: GenerateProcessApiFromFilesystemUseCase.Command
    ): List<GeneratedApiFile> = models.map {
        val modelApi = toBpmnModelApi(it, command, null)
        this.codeGenerator.generateCode(modelApi)
    }

    private fun toBpmnModelApi(
        model: BpmnModel,
        command: GenerateProcessApiFromFilesystemUseCase.Command,
        apiVersion: Int?
    ): BpmnModelApi {
        return BpmnModelApi(
            model = model,
            outputLanguage = command.outputLanguage,
            packagePath = command.packagePath,
            apiVersion = apiVersion
        )
    }

    private fun toBpmnFile(
        bpmnFile: File,
        command: GenerateProcessApiFromFilesystemUseCase.Command
    ) = BpmnResource(
        fileName = bpmnFile.name,
        content = bpmnFile.inputStream(),
        engine = command.engine,
    )

}
