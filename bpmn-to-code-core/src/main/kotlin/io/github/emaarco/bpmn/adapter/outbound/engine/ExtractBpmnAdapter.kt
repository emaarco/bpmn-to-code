package io.github.emaarco.bpmn.adapter.outbound.engine

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.Camunda7ModelExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.EngineSpecificExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.OperatonModelExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.ZeebeModelExtractor
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class ExtractBpmnAdapter(
    private val extractors: Map<ProcessEngine, EngineSpecificExtractor> = ExtractBpmnAdapter.extractors
) : ExtractBpmnPort {

    override fun extract(
        bpmnFile: BpmnResource,
        engine: ProcessEngine,
    ): BpmnModel {
        val content = bpmnFile.content
        val extractor = getExtractor(engine)
        return try {
            logger.info { "Extracting model '${bpmnFile.fileName}' with extractor for '$engine'" }
            extractor.extract(content)
        } catch (ex: Exception) {
            throw RuntimeException(
                "Failed to extract file: ${bpmnFile.fileName}. Please check its a valid file for $engine",
                ex
            )
        }
    }

    private fun getExtractor(engine: ProcessEngine): EngineSpecificExtractor {
        return extractors[engine]
            ?: throw IllegalStateException("No extractor found for engine: $engine")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        val extractors = mapOf(
            ProcessEngine.ZEEBE to ZeebeModelExtractor(),
            ProcessEngine.CAMUNDA_7 to Camunda7ModelExtractor(),
            ProcessEngine.OPERATON to OperatonModelExtractor()
        )
    }
}