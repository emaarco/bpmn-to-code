package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.validation.VariableNameCollisionException
import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import java.util.*

private val logger = KotlinLogging.logger {}

class WebGenerationService {

    private val plugin = CreateProcessApiInMemoryPlugin()

    fun generate(request: GenerateRequest): GenerateResponse {
        val config = request.config
        logger.info { "Generating API for ${request.files.size} file(s) [${config.outputLanguage}, ${config.processEngine}]" }
        try {
            val bpmnInputs = request.files.map { this.buildCommand(it) }
            val generatedApiFiles = this.executePlugin(request.config, bpmnInputs)
            val generatedFiles = generatedApiFiles.map { mapToResponse(it) }
            return GenerateResponse(success = true, files = generatedFiles)
        } catch (e: VariableNameCollisionException) {
            logger.error(e) { "Variable name collision during generation" }
            return GenerateResponse.fromCollisionException(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during generation" }
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
