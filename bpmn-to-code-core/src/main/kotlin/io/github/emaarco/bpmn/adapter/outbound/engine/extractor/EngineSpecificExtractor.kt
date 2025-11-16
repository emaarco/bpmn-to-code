package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.BpmnModel
import java.io.InputStream

interface EngineSpecificExtractor {

    /**
     * Extracts the BPMN model from the given input stream.
     * @param inputStream the input stream to extract the BPMN model from
     */
    fun extract(inputStream: InputStream): BpmnModel
}