package io.github.emaarco.bpmn.application.port.inbound

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationResult

interface ValidateBpmnInMemoryUseCase {

    fun validateBpmn(command: Command): ValidationResult

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
