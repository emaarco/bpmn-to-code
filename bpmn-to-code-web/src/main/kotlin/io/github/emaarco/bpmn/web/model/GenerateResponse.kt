package io.github.emaarco.bpmn.web.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerateResponse(
    val success: Boolean,
    val files: List<GeneratedFile>,
    val error: String? = null
)

@Serializable
data class GeneratedFile(
    val fileName: String,
    val content: String,
    val processId: String
)
