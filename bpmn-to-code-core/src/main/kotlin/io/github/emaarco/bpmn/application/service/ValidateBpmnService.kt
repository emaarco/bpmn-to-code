package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.ValidationResult

class ValidateBpmnService(
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
) : ValidateBpmnFromFilesystemUseCase {

    private val modelMergerService = ModelMergerService()

    override fun validateBpmn(command: ValidateBpmnFromFilesystemUseCase.Command): ValidationResult {
        val validationService = BpmnValidationService(command.validationConfig)
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { file ->
            bpmnService.extract(BpmnResource(file.name, file.inputStream(), command.engine))
        }
        val preMergeViolations = validationService.collectViolations(models, command.engine, ValidationPhase.PRE_MERGE)
        if (preMergeViolations.any { it.severity == Severity.ERROR }) {
            return ValidationResult(preMergeViolations)
        }
        val mergedModels = modelMergerService.mergeModels(models)
        val postMergeViolations = validationService.collectViolations(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        return ValidationResult(preMergeViolations + postMergeViolations)
    }
}
