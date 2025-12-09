package io.github.emaarco.bpmn.web.model

import io.github.emaarco.bpmn.domain.validation.VariableNameCollisionException
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GenerateResponse(
    val success: Boolean,
    val files: List<GeneratedFile>,
    val error: String? = null,
    @Transient val statusCode: HttpStatusCode = HttpStatusCode.OK,
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

        fun unknownError() = GenerateResponse(
            success = false,
            files = emptyList(),
            error = "Unknown error occurred",
            statusCode = HttpStatusCode.InternalServerError
        )

        fun fromCollisionException(
            collisions: VariableNameCollisionException
        ) = GenerateResponse(
            success = false,
            files = emptyList(),
            error = collisions.message,
            statusCode = HttpStatusCode.BadRequest
        )
    }
}
