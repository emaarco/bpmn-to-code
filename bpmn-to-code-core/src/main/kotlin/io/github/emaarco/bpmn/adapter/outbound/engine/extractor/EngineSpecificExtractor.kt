package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.BpmnModel

fun interface EngineSpecificExtractor {

    fun extract(bytes: ByteArray): BpmnModel
}