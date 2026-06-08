package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import org.camunda.bpm.model.xml.instance.DomElement

object DomElementUtils {

    fun List<DomElement>.withElementName(vararg names: String): List<DomElement> {
        return filter { names.contains(it.localName) }
    }

    fun List<DomElement>.withAttribute(pair: Pair<String, String>): List<DomElement> {
        val (attributeName, expectedValue) = pair
        return filter { it.getAttribute(attributeName) == expectedValue }
    }

}
