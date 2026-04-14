package io.github.emaarco.bpmn.domain.validation

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

data class ValidationContext(
    val model: BpmnModel,
    val engine: ProcessEngine,
    val allModels: List<BpmnModel> = emptyList(),
)
