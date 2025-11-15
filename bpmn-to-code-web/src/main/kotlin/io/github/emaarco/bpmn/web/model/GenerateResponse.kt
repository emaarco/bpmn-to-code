package io.github.emaarco.bpmn.web.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerateResponse(
    val success: Boolean,
    val files: List<GeneratedFile>,
    val error: String? = null
) {

    @Serializable
    data class GeneratedFile(
        val fileName: String,
        val content: String,
        val processId: String
    )

    companion object {
        fun noFilesProvided() = GenerateResponse(
            success = false,
            files = emptyList(),
            error = "No files provided"
        )

        fun tooManyFiles() = GenerateResponse(
            success = false,
            files = emptyList(),
            error = "Maximum 3 BPMN files allowed"
        )
    }

}
