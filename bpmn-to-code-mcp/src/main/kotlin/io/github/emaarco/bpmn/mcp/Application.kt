package io.github.emaarco.bpmn.mcp

import io.github.emaarco.bpmn.mcp.tools.registerGenerateProcessApiTool
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting BPMN to Code MCP server..." }
    startStdioServer()
}

internal fun createMcpServer(): Server {
    val server = Server(
        Implementation(name = "bpmn-to-code", version = "0.0.19"),
        ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            ),
        ),
    )
    server.registerGenerateProcessApiTool()
    return server
}

private fun startStdioServer() {
    val server = createMcpServer()
    val transport = StdioServerTransport(System.`in`.asInput(), System.out.asSink().buffered())
    logger.info { "MCP server running on stdio" }
    runBlocking {
        val session = server.createSession(transport)
        val done = Job()
        session.onClose { done.complete() }
        done.join()
    }
}
