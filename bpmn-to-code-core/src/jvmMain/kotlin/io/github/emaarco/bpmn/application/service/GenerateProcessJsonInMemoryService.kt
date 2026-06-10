package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerationAdapter
import io.github.emaarco.bpmn.application.ProcessJsonGeneration
import io.github.emaarco.bpmn.application.port.inbound.GenerateProcessJsonInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedJsonFile

class GenerateProcessJsonInMemoryService(
    private val jsonGenerator: GenerateJsonPort = BpmnJsonGenerationAdapter(),
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
) : GenerateProcessJsonInMemoryUseCase {

    override fun generateProcessJson(
        command: GenerateProcessJsonInMemoryUseCase.Command,
    ): List<GeneratedJsonFile> {
        val bpmnResources = command.bpmnContents.map {
            BpmnResource(fileName = it.processName, content = it.bpmnXml.encodeToByteArray())
        }
        val models = bpmnResources.map { bpmnExtractor.extract(it, command.engine) }
        return ProcessJsonGeneration.generate(
            models = models,
            engine = command.engine,
            validationConfig = command.validationConfig,
            jsonGenerator = jsonGenerator,
        )
    }
}
