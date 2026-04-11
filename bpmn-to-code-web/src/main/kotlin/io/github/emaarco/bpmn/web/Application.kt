@file:OptIn(ExperimentalKtorApi::class)

package io.github.emaarco.bpmn.web

import io.github.emaarco.bpmn.web.config.AppConfig
import io.github.emaarco.bpmn.web.model.ConfigResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.emaarco.bpmn.web.routes.generateJsonRoutes
import io.github.emaarco.bpmn.web.routes.generateRoutes
import io.github.emaarco.bpmn.web.service.WebGenerationService
import io.github.emaarco.bpmn.web.service.WebJsonGenerationService
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
import io.ktor.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

fun main() {

    val appConfig = AppConfig.fromEnvironment()

    embeddedServer(
        factory = Netty,
        port = appConfig.port,
        host = "0.0.0.0",
        module = { configureApp(appConfig) }
    ).start(
        wait = true
    )
}

fun Application.configureApp(
    appConfig: AppConfig
) {

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

        // Configure allowed origins from environment
        if (appConfig.cors.allowsAllOrigins()) {
            anyHost()
        } else {
            appConfig.cors.allowedOrigins.forEach { origin ->
                allowHost(
                    host = origin.removePrefix("https://").removePrefix("http://"),
                    schemes = listOf("https", "http")
                )
            }
        }
    }

    // Call logging
    install(CallLogging)

    // Status pages for error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            val message = mapOf("error" to (cause.message ?: "Unknown error"))
            call.respond(HttpStatusCode.InternalServerError, message)
        }
    }

    // Routing
    routing {
        // Serve static files (frontend)
        staticResources("/static", "static")

        // Serve sample BPMN files
        staticResources("/samples", "samples")

        // Root redirects to static index.html
        get("/") {
            call.respondRedirect("/static/index.html")
        }.hide()

        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }.describe {
            summary = "Health check"
            description = "Returns the health status of the service"
            responses {
                HttpStatusCode.OK { description = "Service is healthy" }
            }
        }

        get("/api/config") {
            val response = ConfigResponse(
                legalLinks = appConfig.legalLinks,
                version = appConfig.version,
            )
            call.respond(response)
        }.describe {
            summary = "Get configuration"
            description = "Returns application configuration including legal links and current version"
            responses {
                HttpStatusCode.OK { description = "Configuration retrieved successfully" }
            }
        }

        // Swagger UI (served from runtime-generated spec)
        swaggerUI("swagger") {
            info = OpenApiInfo("BPMN to Code Web API", appConfig.version)
            source = OpenApiDocSource.Routing(ContentType.Application.Json)
        }

        // API routes
        val generationService = WebGenerationService()
        generateRoutes(generationService)

        val jsonService = WebJsonGenerationService()
        generateJsonRoutes(jsonService)
    }
}
