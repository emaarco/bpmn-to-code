package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.factory.defaultExtractBpmnPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultGenerateApiCodePort
import io.github.emaarco.bpmn.application.ProcessApiGeneration
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedApiFile

class GenerateProcessApiInMemoryService(
    private val codeGenerator: GenerateApiCodePort = defaultGenerateApiCodePort(),
    private val bpmnService: ExtractBpmnPort = defaultExtractBpmnPort(),
) : GenerateProcessApiInMemoryUseCase {

    override fun generateProcessApi(
        command: GenerateProcessApiInMemoryUseCase.Command,
    ): List<GeneratedApiFile> {
        val models = toBpmnFiles(command).map { bpmnService.extract(it, command.engine) }
        return ProcessApiGeneration.generate(
            models = models,
            config = ProcessApiGeneration.Config(
                packagePath = command.packagePath,
                outputLanguage = command.outputLanguage,
                engine = command.engine,
                validationConfig = command.validationConfig,
            ),
            codeGenerator = codeGenerator,
        )
    }

    private fun toBpmnFiles(
        command: GenerateProcessApiInMemoryUseCase.Command,
    ) = command.bpmnContents.map {
        BpmnResource(
            fileName = it.processName,
            content = it.bpmnXml.encodeToByteArray(),
        )
    }
}
