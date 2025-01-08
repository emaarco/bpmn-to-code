package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnModelApi

interface WriteApiFilePort {
    fun writeApiFile(modelApi: BpmnModelApi)
}