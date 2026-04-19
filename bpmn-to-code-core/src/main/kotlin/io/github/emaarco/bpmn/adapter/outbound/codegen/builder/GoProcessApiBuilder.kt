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

// Converts SCREAMING_SNAKE_CASE to PascalCase: "TIMER_AFTER_3_DAYS" → "TimerAfter3Days"
private fun String.toGoPascalCase(): String =
    split('_').joinToString("") { it.lowercase().replaceFirstChar { c -> c.uppercaseChar() } }

class GoProcessApiBuilder : CodeGenerationAdapter.AbstractProcessApiBuilder<StringBuilder>() {

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
        val packageName = modelApi.packagePath.substringAfterLast('.')
        val typesImportPath = modelApi.packagePath.replace('.', '/') + "/types"
        val sb = StringBuilder()

        sb.appendLine("// $autoGenComment")
        sb.appendLine("package $packageName")

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi) }
        val needsTypesImport = relevantWriters.keys.any { it in STRUCT_TYPED_SECTIONS }
        if (needsTypesImport) {
            sb.appendLine()
            sb.appendLine("import \"$typesImportPath\"")
        }

        sb.appendLine()
        sb.appendLine("const (")
        sb.appendLine("\tProcessID     = \"${modelApi.model.processId}\"")
        sb.appendLine("\tProcessEngine = \"${modelApi.engine.name}\"")
        sb.appendLine(")")

        val structWriters = relevantWriters.filter { it.key !in CONST_ONLY_SECTIONS }
        structWriters.forEach { (_, writer) -> writer.write(sb, modelApi) }

        return GeneratedApiFile(
            fileName = "${modelApi.fileName().toGoFileName()}.go",
            packagePath = modelApi.packagePath,
            content = sb.toString(),
            language = modelApi.outputLanguage,
        )
    }

    private fun String.toGoFileName(): String {
        return this
            .replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1_$2")
            .lowercase()
    }

    companion object {
        private val CONST_ONLY_SECTIONS = setOf(ApiObjectType.PROCESS_ID, ApiObjectType.PROCESS_ENGINE)
        private val STRUCT_TYPED_SECTIONS = setOf(
            ApiObjectType.TIMERS, ApiObjectType.ERRORS, ApiObjectType.ESCALATIONS,
            ApiObjectType.FLOWS, ApiObjectType.RELATIONS, ApiObjectType.VARIANTS,
        )
    }

    private class ProcessIdWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(modelApi: BpmnModelApi) = true
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) = Unit
    }

    private class ProcessEngineWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) = Unit
    }

    private fun appendStringStructVar(sb: StringBuilder, groupName: String, entries: List<VariableMapping<String>>) {
        sb.appendLine()
        sb.appendLine("var $groupName = struct {")
        entries.forEach { sb.appendLine("\t${it.getName().toGoPascalCase()} string") }
        sb.appendLine("}{")
        entries.forEach { sb.appendLine("\t${it.getName().toGoPascalCase()}: \"${it.getValue()}\",") }
        sb.appendLine("}")
    }

    private class ElementsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine()
            builder.appendLine("var Elements = struct {")
            modelApi.model.flowNodes.forEach { node ->
                builder.appendLine("\t${node.getName().toGoPascalCase()} string")
            }
            builder.appendLine("}{")
            modelApi.model.flowNodes.forEach { node ->
                builder.appendLine("\t${node.getName().toGoPascalCase()}: \"${node.getValue()}\",")
            }
            builder.appendLine("}")
        }
    }

    private inner class CallActivitiesWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.CALL_ACTIVITIES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.callActivities.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendStringStructVar(builder, "CallActivities", modelApi.model.callActivities)
        }
    }

    private inner class MessagesWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendStringStructVar(builder, "Messages", modelApi.model.messages)
        }
    }

    private inner class ServiceTasksWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(modelApi: BpmnModelApi) =
            modelApi.model.serviceTasks.any { it.getRawName().isNotEmpty() }
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            val tasks = modelApi.model.serviceTasks.filter { it.getRawName().isNotEmpty() }
            appendStringStructVar(builder, "TaskTypes", tasks)
        }
    }

    private class TimersWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.timers.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine()
            builder.appendLine("var Timers = struct {")
            modelApi.model.timers.forEach { timer ->
                builder.appendLine("\t${timer.getName().toGoPascalCase()} types.BpmnTimer")
            }
            builder.appendLine("}{")
            modelApi.model.timers.forEach { timer ->
                val (timerType, timerValue) = timer.getValue()
                builder.appendLine("\t${timer.getName().toGoPascalCase()}: types.BpmnTimer{Type: \"$timerType\", TimerValue: \"$timerValue\"},")
            }
            builder.appendLine("}")
        }
    }

    private class ErrorsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.errors.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine()
            builder.appendLine("var Errors = struct {")
            modelApi.model.errors.forEach { error ->
                builder.appendLine("\t${error.getName().toGoPascalCase()} types.BpmnError")
            }
            builder.appendLine("}{")
            modelApi.model.errors.forEach { error ->
                val (name, code) = error.getValue()
                builder.appendLine("\t${error.getName().toGoPascalCase()}: types.BpmnError{Name: \"$name\", Code: \"$code\"},")
            }
            builder.appendLine("}")
        }
    }

    private class EscalationsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.ESCALATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.escalations.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            builder.appendLine()
            builder.appendLine("var Escalations = struct {")
            modelApi.model.escalations.forEach { esc ->
                builder.appendLine("\t${esc.getName().toGoPascalCase()} types.BpmnEscalation")
            }
            builder.appendLine("}{")
            modelApi.model.escalations.forEach { esc ->
                val (name, code) = esc.getValue()
                builder.appendLine("\t${esc.getName().toGoPascalCase()}: types.BpmnEscalation{Name: \"$name\", Code: \"$code\"},")
            }
            builder.appendLine("}")
        }
    }

    private inner class CompensationsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.COMPENSATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.compensations.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendStringStructVar(builder, "Compensations", modelApi.model.compensations)
        }
    }

    private inner class SignalsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.signals.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendStringStructVar(builder, "Signals", modelApi.model.signals)
        }
    }

    private class VariablesWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            val allVars = modelApi.model.variables
            val nodeVarGroups = modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }

            builder.appendLine()
            builder.appendLine("var Variables = struct {")
            allVars.forEach { v -> builder.appendLine("\t${v.getName().toGoPascalCase()} string") }
            nodeVarGroups.forEach { node ->
                val subName = node.getName().toGoPascalCase()
                builder.appendLine("\t$subName struct {")
                node.variables.sortedBy { it.getRawName() }.forEach { v ->
                    builder.appendLine("\t\t${v.getName().toGoPascalCase()} string")
                }
                builder.appendLine("\t}")
            }
            builder.appendLine("}{")
            allVars.forEach { v -> builder.appendLine("\t${v.getName().toGoPascalCase()}: \"${v.getValue()}\",") }
            nodeVarGroups.forEach { node ->
                val subName = node.getName().toGoPascalCase()
                builder.appendLine("\t$subName: struct {")
                node.variables.sortedBy { it.getRawName() }.forEach { v ->
                    builder.appendLine("\t\t${v.getName().toGoPascalCase()} string")
                }
                builder.appendLine("\t}{")
                node.variables.sortedBy { it.getRawName() }.forEach { v ->
                    builder.appendLine("\t\t${v.getName().toGoPascalCase()}: \"${v.getValue()}\",")
                }
                builder.appendLine("\t},")
            }
            builder.appendLine("}")
        }
    }

    private inner class FlowsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.FLOWS
        override fun shouldWrite(modelApi: BpmnModelApi) =
            modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendFlowsVar(builder, modelApi.model.sequenceFlows)
        }
    }

    private inner class RelationsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) =
            modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            appendRelationsVar(builder, modelApi.model.flowNodes)
        }
    }

    private inner class VariantsWriter : ObjectWriter<StringBuilder> {
        override val objectType = ApiObjectType.VARIANTS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model is MergedBpmnModel
        override fun write(builder: StringBuilder, modelApi: BpmnModelApi) {
            val model = modelApi.model as MergedBpmnModel
            model.variants.forEach { variant ->
                val prefix = variant.variantName.toCamelCase()
                if (variant.sequenceFlows.isNotEmpty()) {
                    appendFlowsVar(builder, variant.sequenceFlows, prefix)
                    appendRelationsVar(builder, variant.flowNodes, prefix)
                }
            }
        }
    }

    private fun appendFlowsVar(sb: StringBuilder, flows: List<SequenceFlowDefinition>, prefix: String = "") {
        val varName = if (prefix.isEmpty()) "Flows" else "${prefix}Flows"
        sb.appendLine()
        sb.appendLine("var $varName = struct {")
        flows.forEach { flow -> sb.appendLine("\t${flow.getName().toGoPascalCase()} types.BpmnFlow") }
        sb.appendLine("}{")
        flows.forEach { flow ->
            sb.appendLine("\t${flow.getName().toGoPascalCase()}: types.BpmnFlow{")
            sb.appendLine("\t\tId:        \"${flow.id ?: ""}\",")
            sb.appendLine("\t\tSourceRef: \"${flow.sourceRef}\",")
            sb.appendLine("\t\tTargetRef: \"${flow.targetRef}\",")
            if (flow.conditionExpression != null) sb.appendLine("\t\tCondition: \"${flow.conditionExpression}\",")
            if (flow.isDefault) sb.appendLine("\t\tIsDefault: true,")
            sb.appendLine("\t},")
        }
        sb.appendLine("}")
    }

    private fun appendRelationsVar(sb: StringBuilder, flowNodes: List<FlowNodeDefinition>, prefix: String = "") {
        val varName = if (prefix.isEmpty()) "Relations" else "${prefix}Relations"
        sb.appendLine()
        sb.appendLine("var $varName = struct {")
        val sortedNodes = flowNodes.filter { it.id != null }.sortedBy { it.getRawName() }
        sortedNodes.forEach { node -> sb.appendLine("\t${node.getName().toGoPascalCase()} types.BpmnRelations") }
        sb.appendLine("}{")
        sortedNodes.forEach { node ->
            sb.appendLine("\t${node.getName().toGoPascalCase()}: types.BpmnRelations{")
            sb.appendLine("\t\tIncoming:         ${goStringSlice(node.incoming)},")
            sb.appendLine("\t\tOutgoing:         ${goStringSlice(node.outgoing)},")
            sb.appendLine("\t\tParentId:         \"${node.parentId ?: ""}\",")
            sb.appendLine("\t\tAttachedToRef:    \"${node.attachedToRef ?: ""}\",")
            sb.appendLine("\t\tAttachedElements: ${goStringSlice(node.attachedElements)},")
            sb.appendLine("\t},")
        }
        sb.appendLine("}")
    }

    private fun goStringSlice(items: List<String>): String {
        if (items.isEmpty()) return "nil"
        return "[]string{${items.joinToString(", ") { "\"$it\"" }}}"
    }
}
