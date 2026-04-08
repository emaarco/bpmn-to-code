package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.github.emaarco.bpmn.application.service.ValidateBpmnService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationResult

class ValidateBpmnFilesystemPlugin(
    private val useCase: ValidateBpmnFromFilesystemUseCase = ValidateBpmnService(),
) {

    fun execute(
        baseDir: String,
        filePattern: String,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): ValidationResult = useCase.validateBpmn(
        ValidateBpmnFromFilesystemUseCase.Command(
            baseDir = baseDir,
            filePattern = filePattern,
            engine = engine,
            validationConfig = validationConfig,
        )
    )
}
