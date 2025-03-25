package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.codegen.WriteApiFileAdapter
import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.versioning.VersionService
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiUseCase
import io.github.emaarco.bpmn.application.port.outbound.ApiVersioningPort
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.WriteApiFilePort
import io.github.emaarco.bpmn.domain.BpmnFile
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import java.io.File

class GenerateProcessApiService(
    private val apiFileWriter: WriteApiFilePort = WriteApiFileAdapter(),
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val versionService: ApiVersioningPort = VersionService(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
) : GenerateProcessApiUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessApi(command: GenerateProcessApiUseCase.Command) {
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val outputFolder = getOutputFolder(command.outputFolderPath)
        val models = inputFiles.map { bpmnService.extract(BpmnFile(it, command.engine)) }
        val mergedModels = modelMergerService.mergeModels(models)
        if (command.useVersioning) {
            createVersionedProcessApis(mergedModels, command, outputFolder)
        } else {
            createUnversionedProcessApis(mergedModels, command, outputFolder)
        }
    }

    private fun createVersionedProcessApis(
        models: List<BpmnModel>,
        command: GenerateProcessApiUseCase.Command,
        outputFolder: File
    ) = models.forEach {
        val currentVersion = this.versionService.getVersion(command.baseDir, it.processId)
        val nextVersion = currentVersion + 1
        val modelApi = toBpmnModelApi(it, command, outputFolder, nextVersion)
        this.apiFileWriter.writeApiFile(modelApi)
        this.versionService.increaseVersion(command.baseDir, it.processId, nextVersion)
    }

    private fun createUnversionedProcessApis(
        models: List<BpmnModel>,
        command: GenerateProcessApiUseCase.Command,
        outputFolder: File
    ) = models.forEach {
        val modelApi = toBpmnModelApi(it, command, outputFolder)
        this.apiFileWriter.writeApiFile(modelApi)
    }

    private fun toBpmnModelApi(
        model: BpmnModel,
        command: GenerateProcessApiUseCase.Command,
        outputFolder: File,
        version: Int? = null
    ) = BpmnModelApi(
        model = model,
        outputLanguage = command.outputLanguage,
        packagePath = command.packagePath,
        outputFolder = outputFolder,
        apiVersion = version,
    )

    private fun getOutputFolder(outputFolderPath: String): File {
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) outputFolder.mkdirs()
        return outputFolder
    }
}
