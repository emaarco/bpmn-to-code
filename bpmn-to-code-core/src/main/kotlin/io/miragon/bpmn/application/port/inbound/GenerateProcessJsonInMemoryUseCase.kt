package io.miragon.bpmn.application.port.inbound

import io.miragon.bpmn.domain.GeneratedJsonFile
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig

interface GenerateProcessJsonInMemoryUseCase {

    fun generateProcessJson(command: Command): List<GeneratedJsonFile>

    data class Command(
        val bpmnContents: List<BpmnInput>,
        val engine: ProcessEngine,
        val validationConfig: ValidationConfig = ValidationConfig(),
    )

    data class BpmnInput(
        val bpmnXml: String,
        val processName: String,
    )
}
