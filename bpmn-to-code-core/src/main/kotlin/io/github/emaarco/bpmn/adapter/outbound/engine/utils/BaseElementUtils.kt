package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import org.camunda.bpm.model.bpmn.instance.BaseElement
import org.camunda.bpm.model.xml.instance.ModelElementInstance

object BaseElementUtils {

    fun BaseElement.findExtensionElement(
        type: String,
    ): ModelElementInstance {
        val extensions = this.findExtensionElementsWithType(type)
        return extensions.firstOrNull() ?: throw IllegalStateException(
            "No extension element of type $type found for flow node ${this.id}"
        )
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