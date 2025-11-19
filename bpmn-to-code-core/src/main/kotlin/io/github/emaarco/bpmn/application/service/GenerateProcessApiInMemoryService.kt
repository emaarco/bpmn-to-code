package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.service.ModelMergerService

class GenerateProcessApiInMemoryService(
    private val logger: LoggerPort,
    private val codeGenerator: GenerateApiCodePort = CodeGenerationAdapter(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(logger),
) : GenerateProcessApiInMemoryUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessApi(
        command: GenerateProcessApiInMemoryUseCase.Command
    ): List<GeneratedApiFile> {
        val modelsAsFiles = toBpmnFiles(command)
        val models = modelsAsFiles.map { bpmnService.extract(it) }
        val mergedModels = modelMergerService.mergeModels(models)
        return mergedModels.map {
            val api = this.toModelApi(command, it)
            this.codeGenerator.generateCode(api)
        }
    }

    private fun toModelApi(
        command: GenerateProcessApiInMemoryUseCase.Command,
        model: BpmnModel
    ) = BpmnModelApi(
        model = model,
        outputLanguage = command.outputLanguage,
        packagePath = command.packagePath,
        apiVersion = null,
    )

    private fun toBpmnFiles(
        command: GenerateProcessApiInMemoryUseCase.Command,
    ) = command.bpmnContents.map {
        BpmnResource(
            fileName = it.processName,
            content = it.bpmnXml.byteInputStream(),
            engine = command.engine,
        )
    }

}
