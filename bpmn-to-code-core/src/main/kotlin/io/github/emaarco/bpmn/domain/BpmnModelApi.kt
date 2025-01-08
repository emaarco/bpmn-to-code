package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import java.io.File

data class BpmnModelApi(
    val model: BpmnModel,
    val outputLanguage: OutputLanguage,
    val packagePath: String,
    val outputFolder: File,
    val apiVersion: Int,
) {

    fun fileName(): String {
        val seperatedProcessId = model.processId.split("_", "-")
        val processId = seperatedProcessId.joinToString("") { it.camelCase() }
        return "${processId}ProcessApiV$apiVersion"
    }

    private fun String.camelCase() = replaceFirstChar { it.uppercase() }

}
