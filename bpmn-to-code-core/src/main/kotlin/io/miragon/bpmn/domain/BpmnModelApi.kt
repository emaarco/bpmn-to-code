package io.miragon.bpmn.domain

import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine

data class BpmnModelApi(
    val model: ProcessModel,
    val outputLanguage: OutputLanguage,
    val packagePath: String,
    val engine: ProcessEngine,
) {

    fun fileName(): String {
        val separatedProcessId = model.processId.split("_", "-")
        val processId = separatedProcessId.joinToString("") { it.camelCase() }
        return "${processId}ProcessApi"
    }

    private fun String.camelCase() = replaceFirstChar { it.uppercase() }

}
