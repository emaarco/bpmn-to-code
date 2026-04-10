package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

interface ExtractBpmnPort {
    fun extract(bpmnFile: BpmnResource, engine: ProcessEngine): BpmnModel
}
