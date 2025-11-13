package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.xml.instance.ModelElementInstance

/**
 * Utility functions for extracting BPMN elements that are common across process engines.
 * Use this only if you have a method that can be used by multiple extractors.
 */
object FlowNodeUtils {

    fun FlowNode.findExtensionElementsWithType(
        type: String,
    ): List<ModelElementInstance> {
        val extensions = this.findExtensionElements()
        return extensions.filter { it.elementType.typeName == type }
    }

    fun FlowNode.findExtensionElements(): List<ModelElementInstance> {
        return this.extensionElements?.elementsQuery?.list() ?: emptyList()
    }
}