package io.miragon.bpmn.adapter.inbound

import io.miragon.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.miragon.bpmn.application.service.GenerateProcessApiService
import io.miragon.bpmn.domain.BpmnFileResult
import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig

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
    ): List<BpmnFileResult> = useCase.generateProcessApi(
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