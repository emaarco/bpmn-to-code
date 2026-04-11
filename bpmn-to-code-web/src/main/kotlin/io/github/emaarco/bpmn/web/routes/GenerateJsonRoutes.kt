@file:OptIn(ExperimentalKtorApi::class)

package io.github.emaarco.bpmn.web.routes

import io.github.emaarco.bpmn.web.model.GenerateJsonRequest
import io.github.emaarco.bpmn.web.model.GenerateJsonResponse
import io.github.emaarco.bpmn.web.service.WebJsonGenerationService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.ExperimentalKtorApi

fun Route.generateJsonRoutes(
    jsonService: WebJsonGenerationService,
) {

    post("/api/generate-json") {

        val request = call.receive<GenerateJsonRequest>()

        if (request.files.isEmpty()) {
            call.respond(status = HttpStatusCode.BadRequest, message = GenerateJsonResponse.noFilesProvided())
            return@post
        } else if (request.files.size > 3) {
            call.respond(status = HttpStatusCode.BadRequest, message = GenerateJsonResponse.tooManyFiles())
            return@post
        }

        val result = jsonService.generate(request)
        call.respond(result.statusCode, result)
    }.describe {
        summary = "Generate process JSON"
        description = "Upload BPMN files and generate JSON representations of the process models"
        responses {
            HttpStatusCode.OK { description = "JSON generated successfully" }
            HttpStatusCode.BadRequest { description = "No files provided, or more than 3 files" }
            HttpStatusCode.InternalServerError { description = "Unexpected error during generation" }
        }
    }
}
