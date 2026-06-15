package io.miragon.bpmn.application.port.outbound

import io.miragon.bpmn.domain.BpmnModelApi
import io.miragon.bpmn.domain.GeneratedApiFile

interface GenerateApiCodePort {
    fun generateCode(modelApi: BpmnModelApi): List<GeneratedApiFile>
}
