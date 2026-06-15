package io.miragon.bpmn.adapter.outbound.json

import io.miragon.bpmn.application.port.outbound.GenerateJsonPort
import io.miragon.bpmn.domain.GeneratedJsonFile
import io.miragon.bpmn.domain.ProcessModel

class BpmnJsonGenerationAdapter(
    private val jsonGenerator: BpmnJsonGenerator = BpmnJsonGenerator(),
) : GenerateJsonPort {

    override fun generateJson(model: ProcessModel): GeneratedJsonFile {
        val json = jsonGenerator.generate(model)
        return GeneratedJsonFile(
            fileName = "${model.processId}.json",
            content = json,
        )
    }
}
