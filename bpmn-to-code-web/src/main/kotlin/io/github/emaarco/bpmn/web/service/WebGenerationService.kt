package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerateResponse
import java.util.Base64

class WebGenerationService(
    private val librarySourceProvider: LibrarySourceProvider = LibrarySourceProvider(),
) {

    private val logger = KotlinLogging.logger {}

    private val plugin = CreateProcessApiInMemoryPlugin()

    fun generate(request: GenerateRequest): GenerateResponse {
        val config = request.config
        logger.info { "Generating API for ${request.files.size} file(s) [${config.outputLanguage}, ${config.processEngine}]" }
        try {
            val bpmnInputs = request.files.map { this.buildCommand(it) }
            val generatedApiFiles = this.executePlugin(request.config, bpmnInputs)
            val generatedFiles = generatedApiFiles.map { mapToResponse(it) }
            return GenerateResponse(
                success = true,
                files = generatedFiles,
                libraryFiles = librarySourceProvider.libraryFiles(),
                runtimeDependency = librarySourceProvider.runtimeDependency(),
            )
        } catch (e: BpmnValidationException) {
            logger.error(e) { "BPMN validation failed during generation" }
            return GenerateResponse.fromValidationException(e)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Unexpected error during generation" }
            return GenerateResponse.unknownError()
        } catch (e: IllegalArgumentException) {
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
        processId = apiFile.processId,
    )
}
