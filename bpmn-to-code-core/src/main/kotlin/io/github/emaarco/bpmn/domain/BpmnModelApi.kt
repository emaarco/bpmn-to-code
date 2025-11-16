package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.OutputLanguage

data class BpmnModelApi(
    val model: BpmnModel,
    val outputLanguage: OutputLanguage,
    val packagePath: String,
    val apiVersion: Int?,
) {

    fun fileName(): String = if (apiVersion != null) {
        "${rawFileName()}V$apiVersion"
    } else {
        rawFileName()
    }

    private fun rawFileName(): String {
        val seperatedProcessId = model.processId.split("_", "-")
        val processId = seperatedProcessId.joinToString("") { it.camelCase() }
        return "${processId}ProcessApi"
    }

    private fun String.camelCase() = replaceFirstChar { it.uppercase() }

}
