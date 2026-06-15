package io.miragon.bpmn.adapter.inbound

import io.miragon.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.miragon.bpmn.application.service.ValidateBpmnService
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig
import io.miragon.bpmn.domain.validation.ValidationResult

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
