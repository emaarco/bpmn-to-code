package io.miragon.bpmn.adapter.outbound.json

import io.miragon.bpmn.adapter.outbound.json.model.BpmnModelJson
import io.miragon.bpmn.domain.ProcessModel
import kotlinx.serialization.json.Json

class BpmnJsonGenerator(
    private val mapper: BpmnJsonMapper = BpmnJsonMapper(),
) {

    private val json = Json { prettyPrint = true }

    fun generate(model: ProcessModel): String {
        val dto = mapper.toJson(model)
        return json.encodeToString(BpmnModelJson.serializer(), dto)
    }
}
