package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.GeneratedJsonFile

interface SaveProcessJsonPort {
    fun writeFiles(generatedFiles: List<GeneratedJsonFile>, outputFolderPath: String)
}
