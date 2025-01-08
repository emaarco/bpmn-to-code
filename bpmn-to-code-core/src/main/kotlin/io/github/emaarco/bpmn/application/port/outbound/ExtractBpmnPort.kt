package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnFile
import io.github.emaarco.bpmn.domain.BpmnModel

interface ExtractBpmnPort {
    fun extract(bpmnFile: BpmnFile): BpmnModel
}
