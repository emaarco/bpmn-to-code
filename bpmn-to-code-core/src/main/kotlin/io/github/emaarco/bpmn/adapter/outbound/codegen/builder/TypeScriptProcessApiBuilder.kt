package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.codegen.writer.ObjectWriter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase

class TypeScriptProcessApiBuilder : CodeGenerationAdapter.AbstractProcessApiBuilder<StringBuilder>() {

    private val objectWriters: Map<ApiObjectType, ObjectWriter<StringBuilder>> = mapOf(
        ApiObjectType.PROCESS_ID to ProcessIdWriter(),
        ApiObjectType.PROCESS_ENGINE to ProcessEngineWriter(),
        ApiObjectType.ELEMENTS to ElementsWriter(),
        ApiObjectType.CALL_ACTIVITIES to CallActivitiesWriter(),
        ApiObjectType.MESSAGES to MessagesWriter(),
        ApiObjectType.SERVICE_TASKS to ServiceTasksWriter(),
        ApiObjectType.TIMERS to TimersWriter(),
        ApiObjectType.ERRORS to ErrorsWriter(),
        ApiObjectType.ESCALATIONS to EscalationsWriter(),
        ApiObjectType.COMPENSATIONS to CompensationsWriter(),
        ApiObjectType.SIGNALS to SignalsWriter(),
        ApiObjectType.VARIABLES to VariablesWriter(),
        ApiObjectType.FLOWS to FlowsWriter(),
        ApiObjectType.RELATIONS to RelationsWriter(),
        ApiObjectType.VARIANTS to VariantsWriter(),
    )

    override fun buildApiFile(modelApi: BpmnModelApi): GeneratedApiFile {
        val objectName = modelApi.fileName()
        val sb = StringBuilder()

        sb.appendLine("// $autoGenComment")
        appendImports(sb, modelApi)

        sb.appendLine("export const $objectName = {")

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi) }
        relevantWriters.forEach { (_, writer) -> writer.write(sb, modelApi) }

        sb.append("};")

        return GeneratedApiFile(
            fileName = "$objectName.ts",
            packagePath = modelApi.packagePath,
            content = sb.toString(),
            language = modelApi.outputLanguage,
        )
    }

    private fun appendImports(sb: StringBuilder, modelApi: BpmnModelApi) {
        val hasErrors = modelApi.model.errors.isNotEmpty()
        val hasEscalations = modelApi.model.escalations.isNotEmpty()
        val hasFlows = modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        val hasMergedFlows = modelApi.model is MergedBpmnModel &&
            modelApi.model.variants.any { it.sequenceFlows.isNotEmpty() }
        val needsFlowTypes = hasFlows || hasMergedFlows
        val hasTimers = modelApi.model.timers.isNotEmpty()

        if (!hasErrors && !hasEscalations && !needsFlowTypes && !hasTimers) {
            sb.appendLine()
            return
        }

        sb.appendLine()
        if (hasErrors) sb.appendLine("import type { BpmnError } from \"./types/BpmnError\";")
        if (hasEscalations) sb.appendLine("import type { BpmnEscalation } from \"./types/BpmnEscalation\";")
        if (needsFlowTypes) {
            sb.appendLine("import type { BpmnFlow } from \"./types/BpmnFlow\";")
            sb.appendLine("import type { BpmnRelations } from \"./types/BpmnRelations\";")
        }
        if (hasTimers) sb.appendLine("import type { BpmnTimer } from \"./types/BpmnTimer\";")
        sb.appendLine()
    }

    private class ProcessIdWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(modelApi: BpmnModelApi) = true
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  PROCESS_ID: \"${modelApi.model.processId}\" as const,")
        }
    }

    private class ProcessEngineWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  PROCESS_ENGINE: \"${modelApi.engine.name}\" as const,")
        }
    }

    private class ElementsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Elements: {")
            modelApi.model.flowNodes.forEach { node ->
                builder.appendLine("    ${node.getName()}: \"${node.getValue()}\",")
            }
            builder.appendLine("  } as const,")
        }
    }

    private class CallActivitiesWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.CALL_ACTIVITIES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.callActivities.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  CallActivities: {")
            modelApi.model.callActivities.forEach { ca ->
                builder.appendLine("    ${ca.getName()}: \"${ca.getValue()}\",")
            }
            builder.appendLine("  } as const,")
        }
    }

    private class MessagesWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Messages: {")
            modelApi.model.messages.forEach { msg ->
                builder.appendLine("    ${msg.getName()}: \"${msg.getValue()}\",")
            }
            builder.appendLine("  } as const,")
        }
    }

    private class ServiceTasksWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(modelApi: BpmnModelApi) =
            modelApi.model.serviceTasks.any { it.getRawName().isNotEmpty() }
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  TaskTypes: {")
            modelApi.model.serviceTasks
                .filter { it.getRawName().isNotEmpty() }
                .forEach { task ->
                    builder.appendLine("    ${task.getName()}: \"${task.getValue()}\",")
                }
            builder.appendLine("  } as const,")
        }
    }

    private class TimersWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.timers.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Timers: {")
            modelApi.model.timers.forEach { timer ->
                val (timerType, timerValue) = timer.getValue()
                builder.appendLine("    ${timer.getName()}: { type: \"$timerType\", timerValue: \"$timerValue\" },")
            }
            builder.appendLine("  } as const satisfies Record<string, BpmnTimer>,")
        }
    }

    private class ErrorsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.errors.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Errors: {")
            modelApi.model.errors.forEach { error ->
                val (name, code) = error.getValue()
                builder.appendLine("    ${error.getName()}: { name: \"$name\", code: \"$code\" },")
            }
            builder.appendLine("  } as const satisfies Record<string, BpmnError>,")
        }
    }

    private class EscalationsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.ESCALATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.escalations.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Escalations: {")
            modelApi.model.escalations.forEach { esc ->
                val (name, code) = esc.getValue()
                builder.appendLine("    ${esc.getName()}: { name: \"$name\", code: \"$code\" },")
            }
            builder.appendLine("  } as const satisfies Record<string, BpmnEscalation>,")
        }
    }

    private class CompensationsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.COMPENSATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.compensations.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Compensations: {")
            modelApi.model.compensations.forEach { comp ->
                builder.appendLine("    ${comp.getName()}: \"${comp.getValue()}\",")
            }
            builder.appendLine("  } as const,")
        }
    }

    private class SignalsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.signals.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Signals: {")
            modelApi.model.signals.forEach { signal ->
                builder.appendLine("    ${signal.getName()}: \"${signal.getValue()}\",")
            }
            builder.appendLine("  } as const,")
        }
    }

    private class VariablesWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine("  Variables: {")
            modelApi.model.variables.forEach { variable ->
                builder.appendLine("    ${variable.getName()}: \"${variable.getValue()}\",")
            }
            modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val subName = node.getRawName().toCamelCase()
                    builder.appendLine("    $subName: {")
                    node.variables.sortedBy { it.getRawName() }.forEach { v ->
                        builder.appendLine("      ${v.getName()}: \"${v.getValue()}\",")
                    }
                    builder.appendLine("    },")
                }
            builder.appendLine("  } as const,")
        }
    }

    private inner class FlowsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.FLOWS
        override fun shouldWrite(modelApi: BpmnModelApi) =
            modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendFlowsBlock(builder, "  ", modelApi.model.sequenceFlows)
        }
    }

    private inner class RelationsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) =
            modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendRelationsBlock(builder, "  ", modelApi.model.flowNodes)
        }
    }

    private inner class VariantsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.VARIANTS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model is MergedBpmnModel
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            val model = modelApi.model as MergedBpmnModel
            builder.appendLine("  Variants: {")
            model.variants.forEach { variant ->
                val variantName = variant.variantName.toCamelCase()
                builder.appendLine("    $variantName: {")
                if (variant.sequenceFlows.isNotEmpty()) {
                    appendFlowsBlock(builder, "      ", variant.sequenceFlows)
                    appendRelationsBlock(builder, "      ", variant.flowNodes)
                }
                builder.appendLine("    },")
            }
            builder.appendLine("  } as const,")
        }
    }

    private fun appendFlowsBlock(sb: StringBuilder, indent: String, flows: List<SequenceFlowDefinition>) {
        sb.appendLine("${indent}Flows: {")
        flows.forEach { flow ->
            sb.append("${indent}  ${flow.getName()}: { id: \"${flow.id ?: ""}\", sourceRef: \"${flow.sourceRef}\", targetRef: \"${flow.targetRef}\"")
            if (flow.conditionExpression != null) sb.append(", condition: \"${flow.conditionExpression}\"")
            if (flow.isDefault) sb.append(", isDefault: true")
            sb.appendLine(" },")
        }
        sb.appendLine("${indent}} as const satisfies Record<string, BpmnFlow>,")
    }

    private fun appendRelationsBlock(sb: StringBuilder, indent: String, flowNodes: List<FlowNodeDefinition>) {
        sb.appendLine("${indent}Relations: {")
        flowNodes
            .filter { it.id != null }
            .sortedBy { it.getRawName() }
            .forEach { node ->
                sb.append("${indent}  ${node.getName()}: { ")
                sb.append("incoming: ${tsStringArray(node.incoming)}, ")
                sb.append("outgoing: ${tsStringArray(node.outgoing)}, ")
                sb.append("parentId: ${tsNullableString(node.parentId)}, ")
                sb.append("attachedToRef: ${tsNullableString(node.attachedToRef)}, ")
                sb.append("attachedElements: ${tsStringArray(node.attachedElements)}")
                sb.appendLine(" },")
            }
        sb.appendLine("${indent}} as const satisfies Record<string, BpmnRelations>,")
    }

    private fun tsStringArray(items: List<String>): String {
        if (items.isEmpty()) return "[]"
        return "[${items.joinToString(", ") { "\"$it\"" }}]"
    }

    private fun tsNullableString(value: String?): String {
        return if (value != null) "\"$value\"" else "null"
    }
}
