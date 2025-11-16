package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import java.io.InputStream

data class BpmnResource(
    val fileName: String,
    val content: InputStream,
    val engine: ProcessEngine,
)
