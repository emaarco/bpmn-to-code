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
        mergedModels.forEach {
            val modelApi = toBpmnModelApi(it, command, outputFolder)
            this.apiFileWriter.writeApiFile(modelApi)
            this.versionService.increaseVersion(command.baseDir, it.processId, modelApi.apiVersion)
        }
    }

    private fun toBpmnModelApi(
        model: BpmnModel,
        command: GenerateProcessApiUseCase.Command,
        outputFolder: File
    ): BpmnModelApi {
        val version = versionService.getVersion(command.baseDir, model.processId)
        return BpmnModelApi(
            model = model,
            outputLanguage = command.outputLanguage,
            packagePath = command.packagePath,
            outputFolder = outputFolder,
            apiVersion = version + 1
        )
    }

    private fun getOutputFolder(outputFolderPath: String): File {
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) outputFolder.mkdirs()
        return outputFolder
    }
}
