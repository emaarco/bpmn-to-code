package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessApiInMemoryUseCase
import io.github.emaarco.bpmn.application.service.GenerateProcessApiInMemoryService
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class CreateProcessApiInMemoryPlugin(
    private val useCase: GenerateProcessApiInMemoryUseCase = GenerateProcessApiInMemoryService(),
) {

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
