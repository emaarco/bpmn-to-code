package io.github.emaarco.bpmn.web.model

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val files: List<BpmnFileData>,
    val config: GenerationConfig
) {

    @Serializable
    data class BpmnFileData(
        val fileName: String,
        val content: String  // Base64-encoded BPMN XML
    )

    @Serializable
    data class GenerationConfig(
        val outputLanguage: OutputLanguage,
        val processEngine: ProcessEngine
    )
}
