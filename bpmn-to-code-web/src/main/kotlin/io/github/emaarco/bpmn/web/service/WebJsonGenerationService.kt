package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessJsonInMemoryPlugin
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.github.emaarco.bpmn.web.model.GenerateJsonRequest
import io.github.emaarco.bpmn.web.model.GenerateJsonResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

class WebJsonGenerationService {

    private val logger = KotlinLogging.logger {}

    private val plugin = CreateProcessJsonInMemoryPlugin()

    fun generate(request: GenerateJsonRequest): GenerateJsonResponse {
        val config = request.config
        logger.info { "Generating JSON for ${request.files.size} file(s) [${config.processEngine}]" }
        try {
            val bpmnInputs = request.files.map { buildInput(it) }
            val jsonFiles = plugin.execute(
                bpmnContents = bpmnInputs,
                engine = config.processEngine,
            )
            val responseFiles = jsonFiles.map { mapToResponse(it) }
            return GenerateJsonResponse(success = true, files = responseFiles)
        } catch (e: BpmnValidationException) {
            logger.error(e) { "BPMN validation failed during JSON generation" }
            return GenerateJsonResponse.fromValidationException(e)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Unexpected error during JSON generation" }
            return GenerateJsonResponse.unknownError()
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Unexpected error during JSON generation" }
            return GenerateJsonResponse.unknownError()
        }
    }

    private fun buildInput(file: GenerateJsonRequest.BpmnFileData): CreateProcessJsonInMemoryPlugin.BpmnInput {
        val bpmnXml = String(Base64.getDecoder().decode(file.content))
        val processName = file.fileName.removeSuffix(".bpmn")
        return CreateProcessJsonInMemoryPlugin.BpmnInput(
            bpmnXml = bpmnXml,
            processName = processName,
        )
    }

    private fun mapToResponse(
        jsonFile: GeneratedJsonFile,
    ) = GenerateJsonResponse.GeneratedJsonFileResponse(
        fileName = jsonFile.fileName,
        content = jsonFile.content,
        processId = jsonFile.fileName.removeSuffix(".json"),
    )
}
