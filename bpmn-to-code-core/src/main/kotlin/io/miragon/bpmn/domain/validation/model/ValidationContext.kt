package io.miragon.bpmn.domain.validation.model

import io.miragon.bpmn.domain.ProcessModel
import io.miragon.bpmn.domain.shared.ProcessEngine

data class ValidationContext(
    val model: ProcessModel,
    val engine: ProcessEngine,
)
