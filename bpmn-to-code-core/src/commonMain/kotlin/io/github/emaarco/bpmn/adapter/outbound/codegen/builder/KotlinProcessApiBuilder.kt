package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.codegen.emitter.ClassRef
import io.github.emaarco.bpmn.adapter.outbound.codegen.emitter.FileSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.emitter.InitializerSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.emitter.KotlinFileEmitter
import io.github.emaarco.bpmn.adapter.outbound.codegen.emitter.PropertySpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.emitter.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.writer.ObjectWriter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase

/**
 * Generates the type-safe API contract for a single BPMN process as a Kotlin object file.
 * References shared BPMN types (BpmnTimer, BpmnError, etc.) from the `bpmn-to-code-runtime` artifact.
 */
class KotlinProcessApiBuilder : CodeGenerationAdapter.AbstractProcessApiBuilder<TypeSpec.Builder>() {

    private companion object {
        const val RUNTIME_PACKAGE = "io.github.emaarco.bpmn.runtime"
        const val KOTLIN_PACKAGE = "kotlin"
        val STRING = ClassRef(KOTLIN_PACKAGE, "String")
        val PROCESS_ID = ClassRef(RUNTIME_PACKAGE, "ProcessId")
        val BPMN_ENGINE = ClassRef(RUNTIME_PACKAGE, "BpmnEngine")
        val ELEMENT_ID = ClassRef(RUNTIME_PACKAGE, "ElementId")
        val MESSAGE_NAME = ClassRef(RUNTIME_PACKAGE, "MessageName")
        val SIGNAL_NAME = ClassRef(RUNTIME_PACKAGE, "SignalName")
        val VARIABLE_NAME = ClassRef(RUNTIME_PACKAGE, "VariableName")
        val BPMN_FLOW = ClassRef(RUNTIME_PACKAGE, "BpmnFlow")
        val BPMN_RELATIONS = ClassRef(RUNTIME_PACKAGE, "BpmnRelations")
        val BPMN_ERROR = ClassRef(RUNTIME_PACKAGE, "BpmnError")
        val BPMN_ESCALATION = ClassRef(RUNTIME_PACKAGE, "BpmnEscalation")
        val BPMN_TIMER = ClassRef(RUNTIME_PACKAGE, "BpmnTimer")
    }

    private val objectWriters: Map<ApiObjectType, ObjectWriter<TypeSpec.Builder>> = mapOf(
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
        val rootBuilder = TypeSpec.Builder(objectName)

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi) }
        relevantWriters.forEach { (_, writer) -> writer.write(rootBuilder, modelApi) }

        val fileSpec = FileSpec(modelApi.packagePath, autoGenComment, rootBuilder.build())
        val content = KotlinFileEmitter.emit(fileSpec)

        return GeneratedApiFile(
            fileName = "$objectName.kt",
            packagePath = modelApi.packagePath,
            content = content,
            language = modelApi.outputLanguage,
            processId = modelApi.model.processId,
        )
    }

    private inner class ProcessIdWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            builder.addProperty(typedProperty("PROCESS_ID", PROCESS_ID, modelApi.model.processId))
        }
    }

    private inner class ProcessEngineWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val initializer = InitializerSpec.ofSingle("BpmnEngine.${modelApi.engine.name}")
            builder.addProperty(PropertySpec("PROCESS_ENGINE", BPMN_ENGINE, initializer))
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val elementsBuilder = TypeSpec.Builder("Elements").addKdoc(
                listOf(
                    "BPMN element ids as declared in the source model.",
                    "Typically used in process-level tests or when searching for tasks.",
                    "Worker runtime code rarely needs these.",
                )
            )
            modelApi.model.flowNodes.forEach { flowNode ->
                elementsBuilder.addProperty(typedProperty(flowNode.getName(), ELEMENT_ID, flowNode.getValue()))
            }
            builder.addType(elementsBuilder.build())
        }
    }

    private inner class FlowsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.FLOWS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean {
            return modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            builder.addType(buildFlowsObject(modelApi.model.sequenceFlows))
        }
    }

    private inner class RelationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean {
            return modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            builder.addType(buildRelationsObject(modelApi.model.flowNodes))
        }
    }

    private inner class VariantsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIANTS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model is MergedBpmnModel

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val model = modelApi.model as? MergedBpmnModel ?: return
            val variantsBuilder = TypeSpec.Builder("Variants")
            model.variants.forEach { variant -> variantsBuilder.addType(buildVariantObject(variant)) }
            builder.addType(variantsBuilder.build())
        }

        private fun buildVariantObject(variant: VariantData): TypeSpec {
            val variantBuilder = TypeSpec.Builder(variant.variantName.toCamelCase())
            if (variant.sequenceFlows.isNotEmpty()) {
                variantBuilder.addType(buildFlowsObject(variant.sequenceFlows))
                variantBuilder.addType(buildRelationsObject(variant.flowNodes))
            }
            return variantBuilder.build()
        }
    }

    private fun buildFlowsObject(sequenceFlows: List<SequenceFlowDefinition>): TypeSpec {
        val flowsBuilder = TypeSpec.Builder("Flows").addKdoc(
            listOf(
                "Sequence flows between BPMN elements.",
                "Mainly useful for process-model tooling, tests, and AI-agent consumers reasoning about the process shape.",
                "Worker code typically does not need these.",
            )
        )
        sequenceFlows.forEach { flow ->
            val initializer = buildFlowInitializer(flow)
            flowsBuilder.addProperty(PropertySpec(flow.getName(), BPMN_FLOW, initializer))
        }
        return flowsBuilder.build()
    }

    private fun buildFlowInitializer(flow: SequenceFlowDefinition): InitializerSpec {
        val lines = mutableListOf("BpmnFlow(")
        lines.add("id = ${stringLiteral(flow.id ?: "")},")
        flow.flowName?.let { lines.add("name = ${stringLiteral(it)},") }
        lines.add("sourceRef = ${stringLiteral(flow.sourceRef)},")
        lines.add("targetRef = ${stringLiteral(flow.targetRef)},")
        flow.conditionExpression?.let { lines.add("condition = ${stringLiteral(it)},") }
        if (flow.isDefault) lines.add("isDefault = true,")
        lines.add(")")
        return InitializerSpec.ofMulti(lines)
    }

    private fun buildRelationsObject(flowNodes: List<FlowNodeDefinition>): TypeSpec {
        val relationsBuilder = TypeSpec.Builder("Relations").addKdoc(
            listOf(
                "Per-element graph metadata (previousElements / followingElements / parentId / boundary attachments).",
                "Intended for tooling and tests, not worker runtime code.",
            )
        )
        flowNodes
            .filter { it.id != null }
            .sortedBy { it.getRawName() }
            .forEach { node ->
                val initializer = buildRelationsInitializer(node)
                relationsBuilder.addProperty(PropertySpec(node.getName(), BPMN_RELATIONS, initializer))
            }
        return relationsBuilder.build()
    }

    private fun buildRelationsInitializer(node: FlowNodeDefinition): InitializerSpec {
        val lines = mutableListOf("BpmnRelations(")
        node.displayName?.let { lines.add("name = ${stringLiteral(it)},") }
        lines.add("previousElements = ${listLiteral(node.previousElements)},")
        lines.add("followingElements = ${listLiteral(node.followingElements)},")
        lines.add("parentId = ${nullableStringLiteral(node.parentId)},")
        lines.add("attachedToRef = ${nullableStringLiteral(node.attachedToRef)},")
        lines.add("attachedElements = ${listLiteral(node.attachedElements)},")
        lines.add(")")
        return InitializerSpec.ofMulti(lines)
    }

    private fun listLiteral(items: List<String>): String {
        return if (items.isEmpty()) "emptyList()" else "listOf(${items.joinToString { "\"$it\"" }})"
    }

    private fun nullableStringLiteral(value: String?): String {
        return if (value != null) "\"$value\"" else "null"
    }

    private inner class CallActivitiesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.CALL_ACTIVITIES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.callActivities.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val callActivitiesBuilder = TypeSpec.Builder("CallActivities")
            modelApi.model.callActivities.forEach { callActivity ->
                callActivitiesBuilder.addProperty(typedProperty(callActivity.getName(), PROCESS_ID, callActivity.getValue()))
            }
            builder.addType(callActivitiesBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val messagesBuilder = TypeSpec.Builder("Messages")
                .addKdoc(listOf("BPMN message names used to correlate messages to running process instances."))
            modelApi.model.messages.forEach { message ->
                messagesBuilder.addProperty(typedProperty(message.getName(), MESSAGE_NAME, message.getValue()))
            }
            builder.addType(messagesBuilder.build())
        }
    }

    /**
     * `ServiceTasks` intentionally emits `const val String` rather than a typed wrapper.
     * Its primary call site is `@JobWorker(type = ServiceTasks.X)` — Kotlin annotation arguments
     * require compile-time constants, which rules out `@JvmInline value class` instances.
     */
    private inner class ServiceTasksWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.serviceTasks.any { it.getRawName().isNotEmpty() }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val tasksBuilder = TypeSpec.Builder("ServiceTasks").addKdoc(
                listOf(
                    "Job worker task types used in `@JobWorker(type = ServiceTasks.X)` annotations.",
                    "Kept as `const val String` because annotation arguments must be compile-time constants.",
                )
            )
            modelApi.model.serviceTasks
                .filter { it.getRawName().isNotEmpty() }
                .forEach { task ->
                    val initializer = InitializerSpec.ofSingle(stringLiteral(task.getValue()))
                    tasksBuilder.addProperty(PropertySpec(task.getName(), STRING, initializer, isConst = true))
                }
            builder.addType(tasksBuilder.build())
        }
    }

    private inner class SignalsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.signals.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val signalsBuilder = TypeSpec.Builder("Signals")
            modelApi.model.signals.forEach { signal ->
                signalsBuilder.addProperty(typedProperty(signal.getName(), SIGNAL_NAME, signal.getValue()))
            }
            builder.addType(signalsBuilder.build())
        }
    }

    private inner class VariablesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val variablesBuilder = TypeSpec.Builder("Variables").addKdoc(
                listOf(
                    "Process variables grouped by the BPMN element that declares them.",
                    "Direction is encoded in each variable's wrapper type: `VariableName.Input`, `VariableName.Output`, or `VariableName.InOut` when the variable is both read and written by the same element.",
                    "Consumer APIs that take a specific subtype (e.g. `fun setOutput(v: VariableName.Output)`) get compile-time direction enforcement.",
                )
            )
            val nodesWithVariables = modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }
            for (node in nodesWithVariables) {
                val nodeVarsBuilder = TypeSpec.Builder(node.getRawName().toCamelCase())
                val variablesByName = node.variables.groupBy { it.getRawName() }
                for (rawName in variablesByName.keys.sorted()) {
                    val group = variablesByName.getValue(rawName)
                    val directions = group.map { it.direction }.toSet()
                    val subtype = VariableNameSubtype.chooseFor(directions)
                    nodeVarsBuilder.addProperty(createDirectionalProperty(group.first(), subtype))
                }
                variablesBuilder.addType(nodeVarsBuilder.build())
            }
            builder.addType(variablesBuilder.build())
        }

        private fun createDirectionalProperty(variable: VariableDefinition, subtype: VariableNameSubtype): PropertySpec {
            val type = VARIABLE_NAME.copy(nested = subtype.simpleName)
            val initializer = InitializerSpec.ofSingle("${type.referencedName()}(${stringLiteral(variable.getValue())})")
            return PropertySpec(variable.getName(), type, initializer)
        }
    }

    private inner class ErrorsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.errors.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val errorsBuilder = TypeSpec.Builder("Errors")
            modelApi.model.errors.forEach {
                val (errorName, errorCode) = it.getValue()
                val initializer = InitializerSpec.ofSingle("BpmnError(\"$errorName\", \"$errorCode\")")
                errorsBuilder.addProperty(PropertySpec(it.getName(), BPMN_ERROR, initializer))
            }
            builder.addType(errorsBuilder.build())
        }
    }

    private inner class EscalationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ESCALATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.escalations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val escalationsBuilder = TypeSpec.Builder("Escalations")
            modelApi.model.escalations.forEach {
                val (escalationName, escalationCode) = it.getValue()
                val initializer = InitializerSpec.ofSingle("BpmnEscalation(\"$escalationName\", \"$escalationCode\")")
                escalationsBuilder.addProperty(PropertySpec(it.getName(), BPMN_ESCALATION, initializer))
            }
            builder.addType(escalationsBuilder.build())
        }
    }

    private inner class CompensationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.COMPENSATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.compensations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val compensationsBuilder = TypeSpec.Builder("Compensations")
            modelApi.model.compensations.forEach { compensation ->
                compensationsBuilder.addProperty(typedProperty(compensation.getName(), ELEMENT_ID, compensation.getValue()))
            }
            builder.addType(compensationsBuilder.build())
        }
    }

    private inner class TimersWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val timersBuilder = TypeSpec.Builder("Timers")
            modelApi.model.timers.forEach { timer ->
                val (timerType, timerValue) = timer.getValue()
                val initializer = InitializerSpec.ofSingle("BpmnTimer(\"$timerType\", ${stringLiteral(timerValue)})")
                timersBuilder.addProperty(PropertySpec(timer.getName(), BPMN_TIMER, initializer))
            }
            builder.addType(timersBuilder.build())
        }
    }

    private fun typedProperty(name: String, type: ClassRef, value: String): PropertySpec {
        val initializer = InitializerSpec.ofSingle("${type.referencedName()}(${stringLiteral(value)})")
        return PropertySpec(name, type, initializer)
    }

    private fun stringLiteral(value: String): String {
        return if (value.contains("\${")) {
            "\$\$\"\"\"$value\"\"\""
        } else {
            "\"$value\""
        }
    }
}
