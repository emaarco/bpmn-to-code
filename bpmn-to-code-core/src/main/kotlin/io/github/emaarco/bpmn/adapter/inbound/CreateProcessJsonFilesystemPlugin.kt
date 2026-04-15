package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonFromFilesystemUseCase
import io.github.emaarco.bpmn.application.service.GenerateProcessJsonService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig

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
