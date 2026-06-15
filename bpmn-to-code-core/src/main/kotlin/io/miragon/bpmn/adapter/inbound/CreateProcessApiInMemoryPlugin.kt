package io.miragon.bpmn.adapter.inbound

import io.miragon.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.miragon.bpmn.application.service.GenerateProcessApiInMemoryService
import io.miragon.bpmn.domain.GeneratedApiFile
import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig

class CreateProcessApiInMemoryPlugin(
    private val useCase: GenerateProcessApiInMemoryUseCase = GenerateProcessApiInMemoryService(),
) {

    fun execute(
        bpmnContents: List<BpmnInput>,
        packagePath: String,
        outputLanguage: OutputLanguage,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): List<GeneratedApiFile> {
        return useCase.generateProcessApi(
            GenerateProcessApiInMemoryUseCase.Command(
                packagePath = packagePath,
                outputLanguage = outputLanguage,
                engine = engine,
                validationConfig = validationConfig,
                bpmnContents = bpmnContents.map {
                    GenerateProcessApiInMemoryUseCase.BpmnInput(
                        bpmnXml = it.bpmnXml,
                        processName = it.processName
                    )
                },
            )
        )
    }

    data class BpmnInput(
        val bpmnXml: String,
        val processName: String,
    )
}
