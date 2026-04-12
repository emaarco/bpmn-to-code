package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.GeneratedJsonFile

interface GenerateJsonPort {
    fun generateJson(model: BpmnModel): GeneratedJsonFile
}
