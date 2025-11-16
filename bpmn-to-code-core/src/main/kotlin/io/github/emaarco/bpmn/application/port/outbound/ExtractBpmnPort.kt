package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.BpmnModel

interface ExtractBpmnPort {
    fun extract(bpmnFile: BpmnResource): BpmnModel
}
