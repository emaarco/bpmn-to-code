package io.github.emaarco.bpmn.web

import io.github.emaarco.bpmn.web.config.AppConfig
import io.github.emaarco.bpmn.web.routes.generateRoutes
import io.github.emaarco.bpmn.web.service.WebGenerationService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {

    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::configureApp
    ).start(
        wait = true
    )
}

fun Application.configureApp() {

    // Load configuration from environment
    val appConfig = AppConfig.fromEnvironment()

    // JSON serialization
    install(ContentNegotiation) {
        val jsonSettings = Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true }
        json(jsonSettings)
    }

    // CORS configuration
    install(CORS) {
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
    }

    // Call logging
    install(CallLogging)

    // Status pages for error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    // Routing
    routing {
        // Serve static files (frontend)
        staticResources("/static", "static")

        // Root redirects to static index.html
        get("/") {
            call.respondRedirect("/static/index.html")
        }

        /**
         * Health check endpoint
         */
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }

        /**
         * Configuration endpoint for the frontend, to get legal links
         */
        get("/api/config") {
            val response = mapOf("legalLinks" to appConfig.legalLinks)
            call.respond(response)
        }

        // Swagger UI endpoint (provides interactive API documentation)
        swaggerUI(path = "swagger", swaggerFile = "openapi/generated.json")

        // API routes
        val generationService = WebGenerationService()
        generateRoutes(generationService)
    }
}
