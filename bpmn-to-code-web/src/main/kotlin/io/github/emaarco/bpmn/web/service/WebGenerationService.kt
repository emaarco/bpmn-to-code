package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import java.util.*

class WebGenerationService {

    private val plugin = CreateProcessApiInMemoryPlugin()

    fun generate(request: GenerateRequest): GenerateResponse {
        try {
            // Decode Base64 BPMN content and prepare input for in-memory plugin
            val bpmnInputs = request.files.map { fileData ->
                val bpmnXml = String(Base64.getDecoder().decode(fileData.content))
                val processName = fileData.fileName.removeSuffix(".bpmn")

                CreateProcessApiInMemoryPlugin.BpmnInput(
                    bpmnXml = bpmnXml,
                    processName = processName
                )
            }

            // Execute in-memory generation
            val generatedApiFiles = plugin.execute(
                bpmnContents = bpmnInputs,
                packagePath = "com.example.process",
                outputLanguage = request.config.outputLanguage,
                engine = request.config.processEngine
            )

            // Map to response format
            val generatedFiles = generatedApiFiles.map { apiFile ->
                GenerateResponse.GeneratedFile(
                    fileName = apiFile.fileName,
                    content = apiFile.content,
                    processId = extractProcessIdFromFileName(apiFile.fileName)
                )
            }

            return GenerateResponse(
                success = true,
                files = generatedFiles
            )

        } catch (e: Exception) {
            return GenerateResponse(
                success = false,
                files = emptyList(),
                error = e.message ?: "Unknown error occurred"
            )
        }
    }

    private fun extractProcessIdFromFileName(fileName: String): String {
        // NewsletterSubscriptionProcessApi.kt -> newsletterSubscription
        // Remove file extension and "ProcessApi" suffix
        return fileName
            .removeSuffix(".kt")
            .removeSuffix(".java")
            .replace(Regex("ProcessApiV\\d+$"), "ProcessApi")  // Handle versioned names
            .removeSuffix("ProcessApi")
            .replaceFirstChar { it.lowercase() }
    }
}
