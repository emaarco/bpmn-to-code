package io.github.emaarco.bpmn.application.port.inbound

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig

interface GenerateProcessJsonFromFilesystemUseCase {

    fun generateProcessJson(command: Command)

    data class Command(
        val baseDir: String,
        val filePattern: String,
        val outputFolderPath: String,
        val engine: ProcessEngine,
        val validationConfig: ValidationConfig = ValidationConfig(),
    )
}
