package io.github.emaarco.bpmn.application.port.outbound

import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.ProcessModel

interface GenerateJsonPort {
    fun generateJson(model: ProcessModel): GeneratedJsonFile
}
