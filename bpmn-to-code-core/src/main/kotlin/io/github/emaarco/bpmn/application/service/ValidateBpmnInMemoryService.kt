package io.github.emaarco.bpmn.application.service

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnInMemoryUseCase
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.ValidationResult

class ValidateBpmnInMemoryService(
    private val bpmnService: ExtractBpmnPort = ExtractBpmnAdapter(),
) : ValidateBpmnInMemoryUseCase {

    private val modelMergerService = ModelMergerService()

    override fun validateBpmn(command: ValidateBpmnInMemoryUseCase.Command): ValidationResult {
        val validationService = BpmnValidationService(command.validationConfig)
        val models = command.bpmnContents.map {
            bpmnService.extract(BpmnResource(it.processName, it.bpmnXml.byteInputStream(), command.engine))
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
