package io.github.emaarco.bpmn.web.model

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import kotlinx.serialization.Serializable

@Serializable
data class GenerateJsonRequest(
    val files: List<BpmnFileData>,
    val config: JsonGenerationConfig,
) {

    @Serializable
    data class BpmnFileData(
        val fileName: String,
        val content: String,
    )

    @Serializable
    data class JsonGenerationConfig(
        val processEngine: ProcessEngine,
    )
}
