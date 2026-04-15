package io.github.emaarco.bpmn.domain.validation.model

import io.github.emaarco.bpmn.domain.ProcessModel
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

data class ValidationContext(
    val model: ProcessModel,
    val engine: ProcessEngine,
)
