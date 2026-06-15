package io.miragon.bpmn.application.port.inbound

import io.miragon.bpmn.domain.BpmnFileResult
import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.validation.model.ValidationConfig

interface GenerateProcessApiFromFilesystemUseCase {
    fun generateProcessApi(command: Command): List<BpmnFileResult>
    data class Command(
        val baseDir: String,
        val filePattern: String,
        val outputFolderPath: String,
        val packagePath: String,
        val outputLanguage: OutputLanguage,
        val engine: ProcessEngine,
        val validationConfig: ValidationConfig = ValidationConfig(),
    )
}