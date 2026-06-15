package io.miragon.bpmn.adapter.inbound

import io.miragon.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.miragon.bpmn.application.service.GenerateProcessJsonInMemoryService
import io.miragon.bpmn.domain.GeneratedJsonFile
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig

class CreateProcessJsonInMemoryPlugin(
    private val useCase: GenerateProcessJsonInMemoryUseCase = GenerateProcessJsonInMemoryService(),
) {

    fun execute(
        bpmnContents: List<BpmnInput>,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): List<GeneratedJsonFile> {
        return useCase.generateProcessJson(
            GenerateProcessJsonInMemoryUseCase.Command(
                engine = engine,
                validationConfig = validationConfig,
                bpmnContents = bpmnContents.map {
                    GenerateProcessJsonInMemoryUseCase.BpmnInput(
                        bpmnXml = it.bpmnXml,
                        processName = it.processName,
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
