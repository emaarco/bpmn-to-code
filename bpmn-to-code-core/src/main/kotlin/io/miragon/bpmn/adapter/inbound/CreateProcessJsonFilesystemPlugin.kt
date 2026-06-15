package io.miragon.bpmn.adapter.inbound

import io.miragon.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.miragon.bpmn.application.service.GenerateProcessJsonService
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig

class CreateProcessJsonFilesystemPlugin(
    private val useCase: GenerateProcessJsonFromFilesystemUseCase = GenerateProcessJsonService(),
) {

    fun execute(
        baseDir: String,
        filePattern: String,
        outputFolderPath: String,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ) = useCase.generateProcessJson(
        GenerateProcessJsonFromFilesystemUseCase.Command(
            baseDir = baseDir,
            filePattern = filePattern,
            outputFolderPath = outputFolderPath,
            engine = engine,
            validationConfig = validationConfig,
        )
    )
}
