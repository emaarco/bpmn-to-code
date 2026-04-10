package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.BpmnResource

interface LoadBpmnFilesPort {
    fun loadFrom(baseDirectory: String, filePattern: String): List<BpmnResource>
}
