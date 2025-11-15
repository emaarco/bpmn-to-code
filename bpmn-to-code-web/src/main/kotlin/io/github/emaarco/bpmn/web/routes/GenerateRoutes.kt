package io.github.emaarco.bpmn.web.routes

import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import io.github.emaarco.bpmn.web.service.WebGenerationService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.generateRoutes(
    generationService: WebGenerationService
) {

    post("/api/generate") {
        try {
            val request = call.receive<GenerateRequest>()

            if (request.files.isEmpty()) {
                call.respond(status = HttpStatusCode.BadRequest, message = GenerateResponse.noFilesProvided())
                return@post
            }

            if (request.files.size > 3) {
                call.respond(status = HttpStatusCode.BadRequest, message = GenerateResponse.tooManyFiles())
                return@post
            }

            // Generate code using core logic
            val result = generationService.generate(request)

            if (result.success) {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.InternalServerError, result)
            }

        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                GenerateResponse(
                    success = false,
                    files = emptyList(),
                    error = "Failed to process request: ${e.message}"
                )
            )
        }
    }
}
