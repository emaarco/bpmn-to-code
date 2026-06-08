package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import org.camunda.bpm.model.xml.instance.ModelElementInstance

object ModelElementInstanceUtils {

    fun List<ModelElementInstance>.findFirstByType(typeName: String): ModelElementInstance? {
        return firstOrNull { it.elementType.typeName == typeName }
    }

    fun List<ModelElementInstance>.filterByType(typeName: String): List<ModelElementInstance> {
        return filter { it.elementType.typeName == typeName }
    }

    fun List<ModelElementInstance>.extractAttribute(attributeName: String): List<String> {
        return mapNotNull { it.domElement.getAttribute(attributeName) }
    }

}
