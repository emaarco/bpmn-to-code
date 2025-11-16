package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile

interface GenerateApiCodePort {
    fun generateCode(modelApi: BpmnModelApi): GeneratedApiFile
}
