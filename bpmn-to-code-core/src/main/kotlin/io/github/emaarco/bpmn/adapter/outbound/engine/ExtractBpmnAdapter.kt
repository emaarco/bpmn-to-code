package io.github.emaarco.bpmn.adapter.outbound.engine

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.Camunda7ModelExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.EngineSpecificExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.OperatonModelExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.ZeebeModelExtractor
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

class ExtractBpmnAdapter(
    private val extractors: Map<ProcessEngine, EngineSpecificExtractor> = ExtractBpmnAdapter.extractors
) : ExtractBpmnPort {

    override fun extract(
        bpmnFile: BpmnResource
    ): BpmnModel {
        val content = bpmnFile.content
        val (engine, extractor) = getExtractor(bpmnFile)
        return try {
            println("Extracting model '${bpmnFile.fileName}' with extractor for '$engine'")
            extractor.extract(content)
        } catch (ex: Exception) {
            throw RuntimeException(
                "Failed to extract file: ${bpmnFile.fileName}. Please check its a valid file for $engine",
                ex
            )
        }
    }

    private fun getExtractor(file: BpmnResource): Map.Entry<ProcessEngine, EngineSpecificExtractor> {
        val entry = extractors.entries.find { it.key == file.engine }
        if (entry == null) throw IllegalStateException("No extractor found for engine: ${file.engine}")
        return entry
    }

    companion object {
        val extractors = mapOf(
            ProcessEngine.ZEEBE to ZeebeModelExtractor(),
            ProcessEngine.CAMUNDA_7 to Camunda7ModelExtractor(),
            ProcessEngine.OPERATON to OperatonModelExtractor()
        )
    }
}