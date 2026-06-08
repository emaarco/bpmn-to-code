package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.factory.defaultExtractBpmnPort
import io.github.emaarco.bpmn.adapter.outbound.factory.defaultLoadBpmnFilesPort
import io.github.emaarco.bpmn.application.ProcessValidation
import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.validation.ValidationResult

class ValidateBpmnService(
    private val bpmnFileLoader: LoadBpmnFilesPort = defaultLoadBpmnFilesPort(),
    private val bpmnService: ExtractBpmnPort = defaultExtractBpmnPort(),
) : ValidateBpmnFromFilesystemUseCase {

    override fun validateBpmn(command: ValidateBpmnFromFilesystemUseCase.Command): ValidationResult {
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnService.extract(it, command.engine) }
        return ProcessValidation.validate(models, command.engine, command.validationConfig)
    }
}
