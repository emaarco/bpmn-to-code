package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiPlugin
import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import io.github.emaarco.bpmn.web.model.GeneratedFile
import java.io.File
import java.util.*
import kotlin.io.path.createTempDirectory

class WebGenerationService {

    private val plugin = CreateProcessApiPlugin()

    fun generate(request: GenerateRequest): GenerateResponse {
        // Create temporary directories for processing
        val tempDir = createTempDirectory("bpmn-to-code-").toFile()
        val outputDir = createTempDirectory("bpmn-output-").toFile()

        try {
            // Write BPMN files to temp directory
            request.files.forEach { fileData ->
                val bpmnContent = Base64.getDecoder().decode(fileData.content)
                val file = File(tempDir, fileData.fileName)
                file.writeBytes(bpmnContent)
            }

            // Execute existing core logic
            plugin.execute(
                baseDir = tempDir.absolutePath,
                filePattern = "**/*.bpmn",
                outputFolderPath = outputDir.absolutePath,
                packagePath = "com.example.process",
                outputLanguage = request.config.outputLanguage,
                engine = request.config.processEngine,
                useVersioning = false
            )

            // Read generated files
            val generatedFiles = outputDir.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .map { file ->
                    GeneratedFile(
                        fileName = file.name,
                        content = file.readText(),
                        processId = extractProcessIdFromFileName(file.name)
                    )
                }
                .toList()

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
        } finally {
            // Cleanup temp directories
            tempDir.deleteRecursively()
            outputDir.deleteRecursively()
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
