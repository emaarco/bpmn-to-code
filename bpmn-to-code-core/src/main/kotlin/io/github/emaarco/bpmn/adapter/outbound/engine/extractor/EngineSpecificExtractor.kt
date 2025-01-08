package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.BpmnModel
import java.io.File

interface EngineSpecificExtractor {

    /**
     * Extracts the BPMN model from the given file.
     * @param file the file to extract the BPMN model from
     */
    fun extract(file: File): BpmnModel
}