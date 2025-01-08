package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import java.io.File

data class BpmnFile(
    val rawFile: File,
    val engine: ProcessEngine,
)
