package io.miragon.bpmn.application.port.outbound

import io.miragon.bpmn.domain.GeneratedApiFile

interface SaveProcessApiPort {
    fun writeFiles(
        generatedFiles: List<GeneratedApiFile>,
        outputFolderPath: String
    )
}
