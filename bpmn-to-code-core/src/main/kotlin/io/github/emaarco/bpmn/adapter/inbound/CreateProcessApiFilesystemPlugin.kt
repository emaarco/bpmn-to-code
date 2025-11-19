package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import io.github.emaarco.bpmn.application.service.GenerateProcessApiService
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class CreateProcessApiFilesystemPlugin(
    private val logger: LoggerPort,
    private val useCase: GenerateProcessApiFromFilesystemUseCase = GenerateProcessApiService(logger),
) {

    /**
     * Secondary constructor for Java compatibility.
     * Creates the plugin with the default use case implementation.
     */
    constructor(logger: LoggerPort) : this(logger, GenerateProcessApiService(logger))

    fun execute(
        baseDir: String,
        filePattern: String,
        outputFolderPath: String,
        packagePath: String,
        outputLanguage: OutputLanguage,
        engine: ProcessEngine,
        useVersioning: Boolean,
    ) = useCase.generateProcessApi(
        GenerateProcessApiFromFilesystemUseCase.Command(
            baseDir = baseDir,
            filePattern = filePattern,
            outputFolderPath = outputFolderPath,
            packagePath = packagePath,
            outputLanguage = outputLanguage,
            engine = engine,
            useVersioning = useVersioning,
        )
    )
}