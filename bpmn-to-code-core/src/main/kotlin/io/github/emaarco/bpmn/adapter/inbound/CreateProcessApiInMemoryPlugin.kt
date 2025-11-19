package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import io.github.emaarco.bpmn.application.service.GenerateProcessApiInMemoryService
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class CreateProcessApiInMemoryPlugin(
    private val logger: LoggerPort,
    private val useCase: GenerateProcessApiInMemoryUseCase = GenerateProcessApiInMemoryService(logger),
) {

    /**
     * Secondary constructor for Java compatibility.
     * Creates the plugin with the default use case implementation.
     */
    constructor(logger: LoggerPort) : this(logger, GenerateProcessApiInMemoryService(logger))

    fun execute(
        bpmnContents: List<BpmnInput>,
        packagePath: String,
        outputLanguage: OutputLanguage,
        engine: ProcessEngine,
    ): List<GeneratedApiFile> {
        return useCase.generateProcessApi(
            GenerateProcessApiInMemoryUseCase.Command(
                packagePath = packagePath,
                outputLanguage = outputLanguage,
                engine = engine,
                bpmnContents = bpmnContents.map {
                    GenerateProcessApiInMemoryUseCase.BpmnInput(
                        bpmnXml = it.bpmnXml,
                        processName = it.processName
                    )
                },
            )
        )
    }

    data class BpmnInput(
        val bpmnXml: String,
        val processName: String,
    )
}
