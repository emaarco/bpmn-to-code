package io.miragon.bpmn.application.port.outbound

import io.miragon.bpmn.domain.BpmnModel
import io.miragon.bpmn.domain.BpmnResource
import io.miragon.bpmn.domain.shared.ProcessEngine

interface ExtractBpmnPort {
    fun extract(bpmnFile: BpmnResource, engine: ProcessEngine): BpmnModel
}
