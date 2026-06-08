package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.BpmnModel

fun interface EngineSpecificExtractor {

    /**
     * Extracts the BPMN model from the given byte array.
     * @param bytes the raw BPMN file content to extract the model from
     */
    fun extract(bytes: ByteArray): BpmnModel
}