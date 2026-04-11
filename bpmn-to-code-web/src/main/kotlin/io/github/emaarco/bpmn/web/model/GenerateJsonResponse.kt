package io.github.emaarco.bpmn.web.model

import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GenerateJsonResponse(
    val success: Boolean,
    val files: List<GeneratedJsonFileResponse>,
    val error: String? = null,
    @Transient val statusCode: HttpStatusCode = HttpStatusCode.OK,
) {

    @Serializable
    data class GeneratedJsonFileResponse(
        val fileName: String,
        val content: String,
        val processId: String,
    )

    companion object {
        fun noFilesProvided() = GenerateJsonResponse(
            success = false,
            files = emptyList(),
            error = "No files provided",
        )

        fun tooManyFiles() = GenerateJsonResponse(
            success = false,
            files = emptyList(),
            error = "Maximum 3 BPMN files allowed",
        )

        fun unknownError() = GenerateJsonResponse(
            success = false,
            files = emptyList(),
            error = "Unknown error occurred",
            statusCode = HttpStatusCode.InternalServerError,
        )

        fun fromValidationException(
            exception: BpmnValidationException,
        ) = GenerateJsonResponse(
            success = false,
            files = emptyList(),
            error = exception.message,
            statusCode = HttpStatusCode.BadRequest,
        )
    }
}
