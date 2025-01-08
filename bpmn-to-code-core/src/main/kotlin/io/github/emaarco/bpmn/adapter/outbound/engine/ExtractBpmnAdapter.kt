package io.github.emaarco.bpmn.adapter.outbound.engine

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.Camunda7ModelExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.EngineSpecificExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.ZeebeModelExtractor
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.domain.BpmnFile
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class ExtractBpmnAdapter(
    private val extractors: Map<ProcessEngine, EngineSpecificExtractor> = ExtractBpmnAdapter.extractors
) : ExtractBpmnPort {

    override fun extract(
        bpmnFile: BpmnFile
    ): BpmnModel {
        val rawFile = bpmnFile.rawFile
        val (engine, extractor) = getExtractor(bpmnFile)
        return try {
            println("Extracting model '${rawFile.name}' with extractor for '$engine'")
            extractor.extract(rawFile)
        } catch (ex: Exception) {
            throw RuntimeException(
                "Failed to extract file: ${rawFile.name}. Please check its a valid file for $engine",
                ex
            )
        }
    }

    private fun getExtractor(file: BpmnFile): Map.Entry<ProcessEngine, EngineSpecificExtractor> {
        val entry = extractors.entries.find { it.key == file.engine }
        if (entry == null) throw IllegalStateException("No extractor found for engine: ${file.engine}")
        return entry
    }

    companion object {
        val extractors = mapOf(
            ProcessEngine.ZEEBE to ZeebeModelExtractor(),
            ProcessEngine.CAMUNDA_7 to Camunda7ModelExtractor()
        )
    }
}