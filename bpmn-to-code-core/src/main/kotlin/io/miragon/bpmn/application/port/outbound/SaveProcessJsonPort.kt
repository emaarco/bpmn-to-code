package io.miragon.bpmn.application.port.outbound

import io.miragon.bpmn.domain.GeneratedJsonFile

interface SaveProcessJsonPort {
    fun writeFiles(generatedFiles: List<GeneratedJsonFile>, outputFolderPath: String)
}
