package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage

class GoSharedTypesBuilder : CodeGenerationAdapter.AbstractSharedTypesBuilder() {

    override fun buildTypeFiles(packagePath: String, language: OutputLanguage): List<GeneratedApiFile> {
        val typesPackage = "$packagePath.types"
        val content = buildString {
            appendLine("// $autoGenComment")
            appendLine("package types")
            appendLine()
            appendLine("type BpmnTimer struct {")
            appendLine("\tType       string")
            appendLine("\tTimerValue string")
            appendLine("}")
            appendLine()
            appendLine("type BpmnError struct {")
            appendLine("\tName string")
            appendLine("\tCode string")
            appendLine("}")
            appendLine()
            appendLine("type BpmnEscalation struct {")
            appendLine("\tName string")
            appendLine("\tCode string")
            appendLine("}")
            appendLine()
            appendLine("type BpmnFlow struct {")
            appendLine("\tId        string")
            appendLine("\tSourceRef string")
            appendLine("\tTargetRef string")
            appendLine("\tCondition string")
            appendLine("\tIsDefault bool")
            appendLine("}")
            appendLine()
            appendLine("type BpmnRelations struct {")
            appendLine("\tIncoming         []string")
            appendLine("\tOutgoing         []string")
            appendLine("\tParentId         string")
            appendLine("\tAttachedToRef    string")
            appendLine("\tAttachedElements []string")
            append("}")
        }
        return listOf(
            GeneratedApiFile(
                fileName = "bpmn_types.go",
                packagePath = typesPackage,
                content = content,
                language = language,
            )
        )
    }
}
