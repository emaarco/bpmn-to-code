package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiUseCase
import io.github.emaarco.bpmn.application.service.GenerateProcessApiService
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class CreateProcessApiPlugin(
    private val useCase: GenerateProcessApiUseCase = GenerateProcessApiService(),
) {

    fun execute(
        baseDir: String,
        filePattern: String,
        outputFolderPath: String,
        packagePath: String,
        outputLanguage: OutputLanguage,
        engine: ProcessEngine
    ) {
        useCase.generateProcessApi(
            GenerateProcessApiUseCase.Command(
                baseDir,
                filePattern,
                outputFolderPath,
                packagePath,
                outputLanguage,
                engine
            )
        )
    }
}