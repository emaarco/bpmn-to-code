package io.miragon.bpmn.adapter.outbound.engine.utils

import org.camunda.bpm.model.bpmn.instance.BaseElement
import org.camunda.bpm.model.xml.instance.ModelElementInstance

object BaseElementUtils {

    fun BaseElement.findExtensionElement(
        type: String,
    ): ModelElementInstance? {
        return this.findExtensionElementsWithType(type).firstOrNull()
    }

    fun BaseElement.findExtensionElementsWithType(
        type: String,
    ): List<ModelElementInstance> {
        val extensions = this.findExtensionElements()
        return extensions.filter { it.elementType.typeName == type }
    }

    fun BaseElement.findExtensionElements(): List<ModelElementInstance> {
        return this.extensionElements?.elementsQuery?.list() ?: emptyList()
    }
}