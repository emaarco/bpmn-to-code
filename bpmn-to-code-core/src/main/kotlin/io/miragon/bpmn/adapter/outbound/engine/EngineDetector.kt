package io.miragon.bpmn.adapter.outbound.engine

import io.miragon.bpmn.domain.shared.ProcessEngine

/**
 * Detects which process engine a BPMN file targets by inspecting its XML namespaces.
 *
 * This is best-effort: it relies on the presence of the engine-specific namespace.
 * It returns `null` for plain BPMN files that carry no engine extensions.
 * The result is used to warn when the selected engine does not match the model's actual target.
 */
object EngineDetector {

    private const val ZEEBE_NS = "http://camunda.org/schema/zeebe/"
    private const val OPERATON_NS = "http://operaton.org/schema/"
    private const val CAMUNDA_7_NS = "http://camunda.org/schema/1.0/bpmn"

    fun detect(content: String): ProcessEngine? = when {
        content.contains(ZEEBE_NS) -> ProcessEngine.ZEEBE
        content.contains(OPERATON_NS) -> ProcessEngine.OPERATON
        content.contains(CAMUNDA_7_NS) -> ProcessEngine.CAMUNDA_7
        else -> null
    }
}
