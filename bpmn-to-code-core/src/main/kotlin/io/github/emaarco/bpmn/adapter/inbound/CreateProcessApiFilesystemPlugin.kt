package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.service.GenerateProcessApiService
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig

class CreateProcessApiFilesystemPlugin(
    private val useCase: GenerateProcessApiFromFilesystemUseCase = GenerateProcessApiService(),
) {

    fun execute(
        baseDir: String,
        filePattern: String,
        outputFolderPath: String,
        packagePath: String,
        outputLanguage: OutputLanguage,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ) = useCase.generateProcessApi(
        GenerateProcessApiFromFilesystemUseCase.Command(
            baseDir = baseDir,
            filePattern = filePattern,
            outputFolderPath = outputFolderPath,
            packagePath = packagePath,
            outputLanguage = outputLanguage,
            engine = engine,
            validationConfig = validationConfig,
        )
    )
}