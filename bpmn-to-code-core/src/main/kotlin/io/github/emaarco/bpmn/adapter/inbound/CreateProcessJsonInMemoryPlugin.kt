package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.github.emaarco.bpmn.application.service.GenerateProcessJsonInMemoryService
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig

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
