package io.github.emaarco.bpmn.mcp.tools

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.DESC_BPMN_XML
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.DESC_OUTPUT_LANGUAGE
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.DESC_PACKAGE_PATH
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.DESC_PROCESS_ENGINE
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.DESC_PROCESS_NAME
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.PARAM_BPMN_XML
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.PARAM_OUTPUT_LANGUAGE
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.PARAM_PACKAGE_PATH
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.PARAM_PROCESS_ENGINE
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.PARAM_PROCESS_NAME
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.TOOL_DESCRIPTION
import io.github.emaarco.bpmn.mcp.tools.GenerateProcessApiToolSchema.TOOL_NAME
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
        name = TOOL_NAME,
        description = TOOL_DESCRIPTION,
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject(PARAM_BPMN_XML) {
                    put("type", "string")
                    put("description", DESC_BPMN_XML)
                }
                putJsonObject(PARAM_PROCESS_NAME) {
                    put("type", "string")
                    put("description", DESC_PROCESS_NAME)
                }
                putJsonObject(PARAM_OUTPUT_LANGUAGE) {
                    put("type", "string")
                    put("description", DESC_OUTPUT_LANGUAGE)
                    putJsonArray("enum") { OutputLanguage.entries.forEach { add(JsonPrimitive(it.name)) } }
                }
                putJsonObject(PARAM_PROCESS_ENGINE) {
                    put("type", "string")
                    put("description", DESC_PROCESS_ENGINE)
                    putJsonArray("enum") { ProcessEngine.entries.forEach { add(JsonPrimitive(it.name)) } }
                }
                putJsonObject(PARAM_PACKAGE_PATH) {
                    put("type", "string")
                    put("description", DESC_PACKAGE_PATH)
                }
            },
            required = listOf(PARAM_BPMN_XML, PARAM_PROCESS_NAME, PARAM_OUTPUT_LANGUAGE, PARAM_PROCESS_ENGINE, PARAM_PACKAGE_PATH),
        ),
        toolAnnotations = ToolAnnotations(readOnlyHint = true),
    ) { request ->
        try {
            val args = request.arguments
                ?: return@addTool errorResult("No arguments provided.")

            val bpmnXml = args[PARAM_BPMN_XML]?.jsonPrimitive?.content
                ?: return@addTool errorResult("Required parameter '$PARAM_BPMN_XML' is missing.")
            val processName = args[PARAM_PROCESS_NAME]?.jsonPrimitive?.content
                ?: return@addTool errorResult("Required parameter '$PARAM_PROCESS_NAME' is missing.")

            val outputLanguage = args[PARAM_OUTPUT_LANGUAGE]?.jsonPrimitive?.content
                ?.let { parseEnum<OutputLanguage>(it, PARAM_OUTPUT_LANGUAGE) }
                ?: return@addTool errorResult("Required parameter '$PARAM_OUTPUT_LANGUAGE' is missing. Valid values: ${OutputLanguage.entries.joinToString { it.name }}")
            val processEngine = args[PARAM_PROCESS_ENGINE]?.jsonPrimitive?.content
                ?.let { parseEnum<ProcessEngine>(it, PARAM_PROCESS_ENGINE) }
                ?: return@addTool errorResult("Required parameter '$PARAM_PROCESS_ENGINE' is missing. Valid values: ${ProcessEngine.entries.joinToString { it.name }}")
            val packagePath = args[PARAM_PACKAGE_PATH]?.jsonPrimitive?.content
                ?: return@addTool errorResult("Required parameter '$PARAM_PACKAGE_PATH' is missing. Please specify the target package (e.g. 'com.example.process').")

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
        } catch (e: BpmnValidationException) {
            errorResult("BPMN validation failed: ${e.message}")
        } catch (e: IllegalArgumentException) {
            errorResult("Invalid argument: ${e.message}")
        } catch (e: IllegalStateException) {
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
