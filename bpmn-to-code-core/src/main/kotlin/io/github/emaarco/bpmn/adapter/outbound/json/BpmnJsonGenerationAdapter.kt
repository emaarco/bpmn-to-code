package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.application.port.outbound.GenerateJsonPort
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.GeneratedJsonFile

class BpmnJsonGenerationAdapter(
    private val jsonGenerator: BpmnJsonGenerator = BpmnJsonGenerator(),
) : GenerateJsonPort {

    override fun generateJson(model: BpmnModel): GeneratedJsonFile {
        val json = jsonGenerator.generate(model)
        val fileName = if (model.variantName != null) {
            "${model.processId}-${model.variantName}.json"
        } else {
            "${model.processId}.json"
        }
        return GeneratedJsonFile(
            fileName = fileName,
            content = json,
        )
    }
}
