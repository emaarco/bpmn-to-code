package io.miragon.bpmn.application.port.outbound

import io.miragon.bpmn.domain.BpmnResource

interface LoadBpmnFilesPort {
    fun loadFrom(baseDirectory: String, filePattern: String): List<BpmnResource>
}
