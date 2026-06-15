package io.miragon.bpmn.application.port.outbound

import io.miragon.bpmn.domain.GeneratedJsonFile
import io.miragon.bpmn.domain.ProcessModel

interface GenerateJsonPort {
    fun generateJson(model: ProcessModel): GeneratedJsonFile
}
