package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

data class BpmnModelApi(
    val model: BpmnModel,
    val outputLanguage: OutputLanguage,
    val packagePath: String,
    val apiVersion: Int?,
    val engine: ProcessEngine,
) {

    fun fileName(): String = if (apiVersion != null) {
        "${rawFileName()}V$apiVersion"
    } else {
        rawFileName()
    }

    private fun rawFileName(): String {
        val separatedProcessId = model.processId.split("_", "-")
        val processId = separatedProcessId.joinToString("") { it.camelCase() }
        return "${processId}ProcessApi"
    }

    private fun String.camelCase() = replaceFirstChar { it.uppercase() }

}
