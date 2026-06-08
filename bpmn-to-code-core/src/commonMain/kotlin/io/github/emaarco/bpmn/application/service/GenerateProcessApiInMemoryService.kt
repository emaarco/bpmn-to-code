package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.factory.defaultExtractBpmnPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultGenerateApiCodePort
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.ProcessModel
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.validation.model.ValidationPhase

class GenerateProcessApiInMemoryService(
    private val codeGenerator: GenerateApiCodePort = defaultGenerateApiCodePort(),
    private val bpmnService: ExtractBpmnPort = defaultExtractBpmnPort(),
) : GenerateProcessApiInMemoryUseCase {

    private val modelMergerService = ModelMergerService()

    override fun generateProcessApi(
        command: GenerateProcessApiInMemoryUseCase.Command
    ): List<GeneratedApiFile> {
        val validationService = BpmnValidationService(command.validationConfig)
        val modelsAsFiles = toBpmnFiles(command)
        val models = modelsAsFiles.map { bpmnService.extract(it, command.engine) }
        validationService.validate(models, command.engine, ValidationPhase.PRE_MERGE)
        val mergedModels = modelMergerService.mergeModels(models)
        validationService.validate(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        return mergedModels
            .flatMap { codeGenerator.generateCode(toModelApi(command, it)) }
            .distinctBy { it.packagePath to it.fileName }
    }

    private fun toModelApi(
        command: GenerateProcessApiInMemoryUseCase.Command,
        model: ProcessModel,
    ) = BpmnModelApi(
        model = model,
        outputLanguage = command.outputLanguage,
        packagePath = command.packagePath,
        engine = command.engine,
    )

    private fun toBpmnFiles(
        command: GenerateProcessApiInMemoryUseCase.Command,
    ) = command.bpmnContents.map {
        BpmnResource(
            fileName = it.processName,
            content = it.bpmnXml.encodeToByteArray(),
        )
    }

}
