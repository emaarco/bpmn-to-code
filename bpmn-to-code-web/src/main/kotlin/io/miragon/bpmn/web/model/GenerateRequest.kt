package io.miragon.bpmn.web.model

import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val files: List<BpmnFileData>,
    val config: GenerationConfig
) {

    @Serializable
    data class BpmnFileData(
        /**
         * The name of the file to generate.
         */
        val fileName: String,

        /**
         * The BPMN XML encoded in Base64.
         */
        val content: String
    )

    @Serializable
    data class GenerationConfig(
        val outputLanguage: OutputLanguage,
        val processEngine: ProcessEngine
    )
}
