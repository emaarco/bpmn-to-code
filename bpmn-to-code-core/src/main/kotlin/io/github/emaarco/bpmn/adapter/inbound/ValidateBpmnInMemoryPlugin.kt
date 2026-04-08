package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.application.port.inbound.ValidateBpmnInMemoryUseCase
import io.github.emaarco.bpmn.application.service.ValidateBpmnInMemoryService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.github.emaarco.bpmn.domain.validation.ValidationViolation

class ValidateBpmnInMemoryPlugin(
    private val useCase: ValidateBpmnInMemoryUseCase = ValidateBpmnInMemoryService(),
) {

    fun validate(
        bpmnXml: String,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): List<ValidationViolation> = execute(
        bpmnContents = listOf(BpmnInput(bpmnXml = bpmnXml, processName = "process")),
        engine = engine,
        validationConfig = validationConfig,
    ).violations

    fun execute(
        bpmnContents: List<BpmnInput>,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ): ValidationResult = useCase.validateBpmn(
        ValidateBpmnInMemoryUseCase.Command(
            engine = engine,
            validationConfig = validationConfig,
            bpmnContents = bpmnContents.map {
                ValidateBpmnInMemoryUseCase.BpmnInput(
                    bpmnXml = it.bpmnXml,
                    processName = it.processName,
                )
            },
        )
    )

    data class BpmnInput(
        val bpmnXml: String,
        val processName: String,
    )
}
