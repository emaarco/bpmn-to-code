package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerator
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.validation.ValidationPhase

class CreateProcessJsonInMemoryPlugin(
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val jsonGenerator: BpmnJsonGenerator = BpmnJsonGenerator(),
) {

    fun execute(
        bpmnContents: List<BpmnInput>,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): List<GeneratedJsonFile> {
        val validationService = BpmnValidationService(validationConfig)
        val bpmnResources = bpmnContents.map { BpmnResource(fileName = it.processName, content = it.bpmnXml.byteInputStream()) }
        val models = bpmnResources.map { bpmnExtractor.extract(it, engine) }
        validationService.validate(models, engine, ValidationPhase.PRE_MERGE)

        return models.map { model ->
            val json = jsonGenerator.generate(model)
            GeneratedJsonFile(
                fileName = "${model.processId}.json",
                content = json,
            )
        }
    }

    data class BpmnInput(
        val bpmnXml: String,
        val processName: String,
    )
}
