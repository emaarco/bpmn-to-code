package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.application.ProcessValidation
import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.validation.ValidationResult

class ValidateBpmnService(
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
) : ValidateBpmnFromFilesystemUseCase {

    override fun validateBpmn(command: ValidateBpmnFromFilesystemUseCase.Command): ValidationResult {
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnService.extract(it, command.engine) }
        return ProcessValidation.validate(models, command.engine, command.validationConfig)
    }
}
