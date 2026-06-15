package io.miragon.bpmn.application.service

import io.miragon.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.miragon.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.miragon.bpmn.application.port.inbound.ValidateBpmnFromFilesystemUseCase
import io.miragon.bpmn.application.port.outbound.ExtractBpmnPort
import io.miragon.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.miragon.bpmn.domain.service.BpmnValidationService
import io.miragon.bpmn.domain.service.ModelMergerService
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationPhase
import io.miragon.bpmn.domain.validation.ValidationResult

class ValidateBpmnService(
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
) : ValidateBpmnFromFilesystemUseCase {

    private val modelMergerService = ModelMergerService()

    override fun validateBpmn(command: ValidateBpmnFromFilesystemUseCase.Command): ValidationResult {
        val validationService = BpmnValidationService(command.validationConfig)
        val inputFiles = bpmnFileLoader.loadFrom(command.baseDir, command.filePattern)
        val models = inputFiles.map { bpmnService.extract(it, command.engine) }
        val preMergeViolations = validationService.collectViolations(models, command.engine, ValidationPhase.PRE_MERGE)
        if (preMergeViolations.any { it.severity == Severity.ERROR }) {
            return ValidationResult(preMergeViolations)
        }
        val mergedModels = modelMergerService.mergeModels(models)
        val postMergeViolations = validationService.collectViolations(mergedModels, command.engine, ValidationPhase.POST_MERGE)
        return ValidationResult(preMergeViolations + postMergeViolations)
    }
}
