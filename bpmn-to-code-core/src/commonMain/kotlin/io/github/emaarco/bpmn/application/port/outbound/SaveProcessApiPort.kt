package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.GeneratedApiFile

interface SaveProcessApiPort {
    fun writeFiles(
        generatedFiles: List<GeneratedApiFile>,
        outputFolderPath: String
    )
}
