package io.github.emaarco.bpmn.application.port.inbound

import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig

interface GenerateProcessApiInMemoryUseCase {
    fun generateProcessApi(command: Command): List<GeneratedApiFile>

    data class Command(
        val bpmnContents: List<BpmnInput>,
        val packagePath: String,
        val outputLanguage: OutputLanguage,
        val engine: ProcessEngine,
        val validationConfig: ValidationConfig = ValidationConfig(),
    )

    data class BpmnInput(
        val bpmnXml: String,
        val processName: String,
    )
}
