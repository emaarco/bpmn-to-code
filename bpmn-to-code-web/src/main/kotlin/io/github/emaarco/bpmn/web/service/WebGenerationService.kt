package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.validation.VariableNameCollisionException
import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import java.util.*

class WebGenerationService {

    private val plugin = CreateProcessApiInMemoryPlugin()

    fun generate(request: GenerateRequest): GenerateResponse {
        try {
            val bpmnInputs = request.files.map { this.buildCommand(it) }
            val generatedApiFiles = this.executePlugin(request.config, bpmnInputs)
            val generatedFiles = generatedApiFiles.map { mapToResponse(it) }
            return GenerateResponse(success = true, files = generatedFiles)
        } catch (e: VariableNameCollisionException) {
            return GenerateResponse.fromCollisionException(e)
        } catch (_: Exception) {
            return GenerateResponse.unknownError()
        }
    }

    private fun executePlugin(
        config: GenerateRequest.GenerationConfig,
        bpmnContents: List<CreateProcessApiInMemoryPlugin.BpmnInput>,
    ) = plugin.execute(
        bpmnContents = bpmnContents,
        packagePath = "com.example.process",
        outputLanguage = config.outputLanguage,
        engine = config.processEngine
    )

    private fun buildCommand(file: GenerateRequest.BpmnFileData): CreateProcessApiInMemoryPlugin.BpmnInput {
        val bpmnXml = String(Base64.getDecoder().decode(file.content))
        val processName = file.fileName.removeSuffix(".bpmn")
        return CreateProcessApiInMemoryPlugin.BpmnInput(
            bpmnXml = bpmnXml,
            processName = processName
        )
    }

    private fun mapToResponse(
        apiFile: GeneratedApiFile
    ) = GenerateResponse.GeneratedFile(
        fileName = apiFile.fileName,
        content = apiFile.content,
        processId = extractProcessIdFromFileName(apiFile.fileName)
    )

    /**
     * NewsletterSubscriptionProcessApi.kt -> newsletterSubscription
     * Remove file extension and "ProcessApi" suffix
     */
    private fun extractProcessIdFromFileName(
        fileName: String
    ): String {
        return fileName
            .removeSuffix(".kt")
            .removeSuffix(".java")
            .replace(Regex("ProcessApiV\\d+$"), "ProcessApi")  // Handle versioned names
            .removeSuffix("ProcessApi")
            .replaceFirstChar { it.lowercase() }
    }
}
