package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage

class TypeScriptSharedTypesBuilder : CodeGenerationAdapter.AbstractSharedTypesBuilder() {

    override fun buildTypeFiles(packagePath: String, language: OutputLanguage): List<GeneratedApiFile> {
        val typesPackage = "$packagePath.types"
        return listOf(
            GeneratedApiFile(
                fileName = "ProcessApi.ts",
                packagePath = typesPackage,
                content = buildProcessApiContent(),
                language = language,
            )
        )
    }

    private fun buildProcessApiContent() = buildString {
        appendLine("// $autoGenComment")
        appendLine()
        appendLine("export interface BpmnTimer {")
        appendLine("  type: string;")
        appendLine("  timerValue: string;")
        appendLine("}")
        appendLine()
        appendLine("export interface BpmnError {")
        appendLine("  name: string;")
        appendLine("  code: string;")
        appendLine("}")
        appendLine()
        appendLine("export interface BpmnEscalation {")
        appendLine("  name: string;")
        appendLine("  code: string;")
        appendLine("}")
        appendLine()
        appendLine("export interface BpmnFlow {")
        appendLine("  id: string;")
        appendLine("  sourceRef: string;")
        appendLine("  targetRef: string;")
        appendLine("  condition?: string;")
        appendLine("  isDefault?: boolean;")
        appendLine("}")
        appendLine()
        appendLine("export interface BpmnRelations {")
        appendLine("  incoming: string[];")
        appendLine("  outgoing: string[];")
        appendLine("  parentId: string | null;")
        appendLine("  attachedToRef: string | null;")
        appendLine("  attachedElements: string[];")
        appendLine("}")
        appendLine()
        appendLine("export interface ProcessApi {")
        appendLine("  PROCESS_ID: string;")
        appendLine("  PROCESS_ENGINE: string;")
        appendLine("  Elements: Record<string, string>;")
        appendLine("  CallActivities?: Record<string, string>;")
        appendLine("  Messages?: Record<string, string>;")
        appendLine("  TaskTypes?: Record<string, string>;")
        appendLine("  Timers?: Record<string, BpmnTimer>;")
        appendLine("  Errors?: Record<string, BpmnError>;")
        appendLine("  Escalations?: Record<string, BpmnEscalation>;")
        appendLine("  Compensations?: Record<string, string>;")
        appendLine("  Signals?: Record<string, string>;")
        appendLine("  Variables?: Record<string, Record<string, string>>;")
        appendLine("  Flows?: Record<string, BpmnFlow>;")
        appendLine("  Relations?: Record<string, BpmnRelations>;")
        appendLine("  Variants?: Record<string, {")
        appendLine("    Flows?: Record<string, BpmnFlow>;")
        appendLine("    Relations?: Record<string, BpmnRelations>;")
        appendLine("  }>;")
        append("}")
    }
}
