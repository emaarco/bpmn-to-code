@file:OptIn(ExperimentalKtorApi::class)

package io.github.emaarco.bpmn.web.routes

import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import io.github.emaarco.bpmn.web.service.WebGenerationService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.ExperimentalKtorApi

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
    }.describe {
        summary = "Generate process API code"
        description = "Upload BPMN files and generate type-safe API code for Camunda 7 or Zeebe"
        responses {
            HttpStatusCode.OK { description = "Code generated successfully" }
            HttpStatusCode.BadRequest { description = "No files provided, or more than 3 files" }
            HttpStatusCode.InternalServerError { description = "Unexpected error during generation" }
        }
    }
}
