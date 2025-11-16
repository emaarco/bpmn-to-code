package io.github.emaarco.bpmn.application.port.inbound

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

interface GenerateProcessApiFromFilesystemUseCase {
    fun generateProcessApi(command: Command)
    data class Command(
        val baseDir: String,
        val filePattern: String,
        val outputFolderPath: String,
        val packagePath: String,
        val outputLanguage: OutputLanguage,
        val engine: ProcessEngine,
        val useVersioning: Boolean = false,
    )
}