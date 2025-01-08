package io.github.emaarco.bpmn.application.port.outbound

import java.io.File

interface LoadBpmnFilesPort {
    fun loadFrom(baseDirectory: String, filePattern: String): List<File>
}
