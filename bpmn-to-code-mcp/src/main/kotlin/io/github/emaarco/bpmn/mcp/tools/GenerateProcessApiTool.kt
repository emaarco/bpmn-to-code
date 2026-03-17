package io.github.emaarco.bpmn.mcp.tools

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.VariableNameCollisionException
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Stateless, thread-safe singleton — safe for concurrent use in HTTP mode.
 */
private val plugin = CreateProcessApiInMemoryPlugin()

fun Server.registerGenerateProcessApiTool() {
    addTool(
        name = "generate_process_api",
        description = "Generate type-safe process API code from BPMN XML. " +
                "Returns generated source files for interacting with BPMN process tasks and variables.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("bpmnXml") {
                    put("type", "string")
                    put("description", "Raw BPMN XML content")
                }
                putJsonObject("processName") {
                    put("type", "string")
                    put(
                        "description",
                        "Process identifier used for naming generated classes (e.g. 'newsletter-subscription')"
                    )
                }
                putJsonObject("outputLanguage") {
                    put("type", "string")
                    put("description", "Output language: KOTLIN or JAVA")
                    putJsonArray("enum") { add(JsonPrimitive("KOTLIN")); add(JsonPrimitive("JAVA")) }
                }
                putJsonObject("processEngine") {
                    put("type", "string")
                    put("description", "Target process engine: ZEEBE, CAMUNDA_7, or OPERATON")
                    putJsonArray("enum") {
                        add(JsonPrimitive("ZEEBE")); add(JsonPrimitive("CAMUNDA_7")); add(
                        JsonPrimitive("OPERATON")
                    )
                    }
                }
                putJsonObject("packagePath") {
                    put("type", "string")
                    put("description", "Target package for generated code (e.g. 'com.example.process')")
                }
            },
            required = listOf("bpmnXml", "processName"),
        ),
        toolAnnotations = ToolAnnotations(readOnlyHint = true),
    ) { request ->
        try {
            val args = request.arguments
                ?: return@addTool errorResult("No arguments provided.")

            val bpmnXml = args["bpmnXml"]?.jsonPrimitive?.content
                ?: return@addTool errorResult("Required parameter 'bpmnXml' is missing.")
            val processName = args["processName"]?.jsonPrimitive?.content
                ?: return@addTool errorResult("Required parameter 'processName' is missing.")

            val outputLanguage = args["outputLanguage"]?.jsonPrimitive?.content
                ?.let { parseEnum<OutputLanguage>(it, "outputLanguage") }
                ?: OutputLanguage.KOTLIN
            val processEngine = args["processEngine"]?.jsonPrimitive?.content
                ?.let { parseEnum<ProcessEngine>(it, "processEngine") }
                ?: ProcessEngine.ZEEBE
            val packagePath = args["packagePath"]?.jsonPrimitive?.content
                ?: "com.example.process"

            val generatedFiles = plugin.execute(
                bpmnContents = listOf(
                    CreateProcessApiInMemoryPlugin.BpmnInput(
                        bpmnXml = bpmnXml,
                        processName = processName,
                    )
                ),
                packagePath = packagePath,
                outputLanguage = outputLanguage,
                engine = processEngine,
            )

            val output = generatedFiles.joinToString("\n\n") { file ->
                "// === ${file.fileName} ===\n${file.content}"
            }

            CallToolResult(content = listOf(TextContent(output)))
        } catch (e: VariableNameCollisionException) {
            errorResult("Variable name collision: ${e.message}")
        } catch (e: IllegalArgumentException) {
            errorResult("Invalid argument: ${e.message}")
        } catch (e: Exception) {
            errorResult("Generation failed: ${e.message}")
        }
    }
}

private inline fun <reified T : Enum<T>> parseEnum(value: String, paramName: String): T {
    return try {
        enumValueOf<T>(value.uppercase())
    } catch (_: IllegalArgumentException) {
        val validValues = enumValues<T>().joinToString(", ") { it.name }
        throw IllegalArgumentException("Invalid value '$value' for '$paramName'. Valid values: $validValues")
    }
}

private fun errorResult(message: String) = CallToolResult(
    content = listOf(TextContent(message)),
    isError = true,
)
