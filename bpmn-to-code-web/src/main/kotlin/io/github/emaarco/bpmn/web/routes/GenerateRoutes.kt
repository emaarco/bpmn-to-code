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

        val request = call.receive<GenerateRequest>()

        if (request.files.isEmpty()) {
            call.respond(status = HttpStatusCode.BadRequest, message = GenerateResponse.noFilesProvided())
            return@post
        } else if (request.files.size > 3) {
            call.respond(status = HttpStatusCode.BadRequest, message = GenerateResponse.tooManyFiles())
            return@post
        }

        val result = generationService.generate(request)
        call.respond(result.statusCode, result)
    }
}
