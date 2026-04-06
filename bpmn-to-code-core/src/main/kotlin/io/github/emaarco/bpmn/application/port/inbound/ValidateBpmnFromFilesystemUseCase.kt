package io.github.emaarco.bpmn.application.port.inbound

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationResult

interface ValidateBpmnFromFilesystemUseCase {

    fun validateBpmn(command: Command): ValidationResult

    data class Command(
        val baseDir: String,
        val filePattern: String,
        val engine: ProcessEngine,
        val validationConfig: ValidationConfig = ValidationConfig(),
    )
}
