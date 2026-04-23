package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDirection
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase

/**
 * Generates the type-safe API contract for a single BPMN process as a Kotlin object file.
 * References shared BPMN types (BpmnTimer, BpmnError, etc.) from the sibling `types/` package via import.
 */
class KotlinProcessApiBuilder : CodeGenerationAdapter.AbstractProcessApiBuilder<TypeSpec.Builder>() {

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
        val unusedAnnotation = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
        val rootObjectBuilder = TypeSpec.objectBuilder(objectName)
        val fileSpecBuilder = FileSpec.builder(modelApi.packagePath, objectName).addFileComment(autoGenComment)

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi) }
        relevantWriters.forEach { (_, writer) -> writer.write(rootObjectBuilder, modelApi) }

        fileSpecBuilder.addType(rootObjectBuilder.build()).addAnnotation(unusedAnnotation)
        val fileSpec = fileSpecBuilder.build()

        val content = buildString { fileSpec.writeTo(this) }.replace("public ", "")

        return GeneratedApiFile(
            fileName = "$objectName.kt",
            packagePath = modelApi.packagePath,
            content = content,
            language = modelApi.outputLanguage
        )
    }

    private inner class ProcessIdWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val processIdClass = ClassName("${modelApi.packagePath}.types", "ProcessId")
            val cleanValue = modelApi.model.processId.escapeDollarInterpolation()
            val idProperty = PropertySpec.builder("PROCESS_ID", processIdClass)
                .initializer("ProcessId(\"$cleanValue\")")
                .build()
            builder.addProperty(idProperty)
        }
    }

    private class ProcessEngineWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnEngineClass = ClassName("${modelApi.packagePath}.types", "BpmnEngine")
            val engineProperty = PropertySpec.builder("PROCESS_ENGINE", bpmnEngineClass)
                .initializer("%T.%L", bpmnEngineClass, modelApi.engine.name)
                .build()
            builder.addProperty(engineProperty)
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val elementIdClass = ClassName("${modelApi.packagePath}.types", "ElementId")
            val elementsBuilder = TypeSpec.objectBuilder("Elements")
                .addKdoc(
                    "BPMN element ids as declared in the source model.\n" +
                        "Typically used in process-level tests or when searching for tasks.\n" +
                        "Worker runtime code rarely needs these."
                )
            modelApi.model.flowNodes.forEach { flowNode ->
                elementsBuilder.addProperty(createTypedAttribute(flowNode, elementIdClass))
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
            val flowsObject = buildFlowsObject(modelApi.packagePath, modelApi.model.sequenceFlows)
            builder.addType(flowsObject)
        }
    }

    private inner class RelationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean {
            return modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val relationsObject = buildRelationsObject(modelApi.packagePath, modelApi.model.flowNodes)
            builder.addType(relationsObject)
        }
    }

    private inner class VariantsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIANTS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model is MergedBpmnModel

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val model = modelApi.model as MergedBpmnModel
            val variantsBuilder = TypeSpec.objectBuilder("Variants")
            model.variants.forEach { variant ->
                val variantObject = buildVariantObject(modelApi.packagePath, variant)
                variantsBuilder.addType(variantObject)
            }
            builder.addType(variantsBuilder.build())
        }

        private fun buildVariantObject(packagePath: String, variant: VariantData): TypeSpec {
            val variantName = variant.variantName.toCamelCase()
            val variantBuilder = TypeSpec.objectBuilder(variantName)
            if (variant.sequenceFlows.isNotEmpty()) {
                variantBuilder.addType(buildFlowsObject(packagePath, variant.sequenceFlows))
                variantBuilder.addType(buildRelationsObject(packagePath, variant.flowNodes))
            }
            return variantBuilder.build()
        }
    }

    private fun buildFlowsObject(packagePath: String, sequenceFlows: List<SequenceFlowDefinition>): TypeSpec {
        val bpmnFlowClass = ClassName("${packagePath}.types", "BpmnFlow")
        val flowsBuilder = TypeSpec.objectBuilder("Flows")
            .addKdoc(
                "Sequence flows between BPMN elements.\n" +
                    "Mainly useful for process-model tooling, tests, and AI-agent consumers reasoning about the process shape.\n" +
                    "Worker code typically does not need these."
            )
        sequenceFlows.forEach { flow ->
            val initStr = buildFlowInitializer(flow.id ?: "", flow.flowName, flow.sourceRef, flow.targetRef, flow.conditionExpression, flow.isDefault)
            flowsBuilder.addProperty(PropertySpec.builder(flow.getName(), bpmnFlowClass).initializer(initStr).build())
        }
        return flowsBuilder.build()
    }

    private fun buildFlowInitializer(id: String, name: String?, sourceRef: String, targetRef: String, condition: String?, isDefault: Boolean): CodeBlock {
        return CodeBlock.builder().apply {
            add("BpmnFlow(\n")
            indent()
            add("id = %S,\n", id)
            if (name != null) add("name = %S,\n", name)
            add("sourceRef = %S,\n", sourceRef)
            add("targetRef = %S,\n", targetRef)
            if (condition != null) add("condition = \"${condition.escapeDollarInterpolation()}\",\n")
            if (isDefault) add("isDefault = true,\n")
            unindent()
            add(")")
        }.build()
    }

    private fun buildRelationsObject(packagePath: String, flowNodes: List<FlowNodeDefinition>): TypeSpec {
        val bpmnRelationsClass = ClassName("${packagePath}.types", "BpmnRelations")
        val relationsBuilder = TypeSpec.objectBuilder("Relations")
            .addKdoc(
                "Per-element graph metadata (previousElements / followingElements / parentId / boundary attachments).\n" +
                    "Intended for tooling and tests, not worker runtime code."
            )
        flowNodes
            .filter { it.id != null }
            .sortedBy { it.getRawName() }
            .forEach { node ->
                val initStr = buildRelationsInitializer(node)
                relationsBuilder.addProperty(PropertySpec.builder(node.getName(), bpmnRelationsClass).initializer(initStr).build())
            }
        return relationsBuilder.build()
    }

    private fun buildRelationsInitializer(node: FlowNodeDefinition): CodeBlock {
        return CodeBlock.builder().apply {
            add("BpmnRelations(\n")
            indent()
            if (node.displayName != null) add("name = %S,\n", node.displayName)
            add("previousElements = %L,\n", listLiteral(node.previousElements))
            add("followingElements = %L,\n", listLiteral(node.followingElements))
            add("parentId = %L,\n", nullableStringLiteral(node.parentId))
            add("attachedToRef = %L,\n", nullableStringLiteral(node.attachedToRef))
            add("attachedElements = %L,\n", listLiteral(node.attachedElements))
            unindent()
            add(")")
        }.build()
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
            val processIdClass = ClassName("${modelApi.packagePath}.types", "ProcessId")
            val callActivitiesBuilder = TypeSpec.objectBuilder("CallActivities")
            modelApi.model.callActivities.forEach { callActivity ->
                callActivitiesBuilder.addProperty(createTypedAttribute(callActivity, processIdClass))
            }
            builder.addType(callActivitiesBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val messageNameClass = ClassName("${modelApi.packagePath}.types", "MessageName")
            val messagesBuilder = TypeSpec.objectBuilder("Messages")
                .addKdoc("BPMN message names used to correlate messages to running process instances.")
            modelApi.model.messages.forEach { message ->
                messagesBuilder.addProperty(createTypedAttribute(message, messageNameClass))
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
            val tasksBuilder = TypeSpec.objectBuilder("ServiceTasks")
                .addKdoc(
                    "Job worker task types used in `@JobWorker(type = ServiceTasks.X)` annotations.\n" +
                        "Kept as `const val String` because annotation arguments must be compile-time constants."
                )
            modelApi.model.serviceTasks
                .filter { it.getRawName().isNotEmpty() }
                .forEach { task -> tasksBuilder.addProperty(createAttribute(task)) }
            builder.addType(tasksBuilder.build())
        }
    }

    private inner class SignalsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.signals.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val signalNameClass = ClassName("${modelApi.packagePath}.types", "SignalName")
            val signalsBuilder = TypeSpec.objectBuilder("Signals")
            modelApi.model.signals.forEach { signal ->
                signalsBuilder.addProperty(createTypedAttribute(signal, signalNameClass))
            }
            builder.addType(signalsBuilder.build())
        }
    }

    private inner class VariablesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val variableNameClass = ClassName("${modelApi.packagePath}.types", "VariableName")
            val variablesBuilder = TypeSpec.objectBuilder("Variables")
                .addKdoc(
                    "Process variables grouped by the BPMN element that declares them.\n" +
                        "Direction is encoded in each variable's wrapper type: `VariableName.Input`, `VariableName.Output`, or `VariableName.InOut` when the variable is both read and written by the same element.\n" +
                        "Consumer APIs that take a specific subtype (e.g. `fun setOutput(v: VariableName.Output)`) get compile-time direction enforcement."
                )
            modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val objectName = (node.getRawName()).toCamelCase()
                    val nodeVarsBuilder = TypeSpec.objectBuilder(objectName)
                    collapseByDirection(node.variables)
                        .sortedBy { (rawName, _) -> rawName }
                        .forEach { (_, entry) ->
                            nodeVarsBuilder.addProperty(createDirectionalAttribute(entry, variableNameClass))
                        }
                    variablesBuilder.addType(nodeVarsBuilder.build())
                }
            builder.addType(variablesBuilder.build())
        }

        private fun collapseByDirection(variables: List<VariableDefinition>): List<Pair<String, CollapsedVariable>> {
            val byName = variables.groupBy { it.getRawName() }
            return byName.entries.map { (rawName, group) ->
                val directions = group.map { it.direction }.toSet()
                val subtype = when {
                    directions.containsAll(setOf(VariableDirection.INPUT, VariableDirection.OUTPUT)) -> "InOut"
                    directions.contains(VariableDirection.INPUT) -> "Input"
                    else -> "Output"
                }
                rawName to CollapsedVariable(group.first(), subtype)
            }
        }

        private fun createDirectionalAttribute(entry: CollapsedVariable, wrapperClass: ClassName): PropertySpec {
            val cleanValue = entry.definition.getValue().escapeDollarInterpolation()
            val subtypeClass = wrapperClass.nestedClass(entry.subtype)
            return PropertySpec.builder(entry.definition.getName(), subtypeClass)
                .initializer("%T(\"$cleanValue\")", subtypeClass)
                .build()
        }
    }

    private data class CollapsedVariable(
        val definition: VariableDefinition,
        val subtype: String,
    )

    private class ErrorsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.errors.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnErrorClass = ClassName("${modelApi.packagePath}.types", "BpmnError")
            val errorsBuilder = TypeSpec.objectBuilder("Errors")
            modelApi.model.errors.forEach {
                val (errorName, errorCode) = it.getValue()
                val instanceBuilder = PropertySpec.builder(it.getName(), bpmnErrorClass)
                val variable = instanceBuilder.initializer("BpmnError(\"$errorName\", \"$errorCode\")")
                errorsBuilder.addProperty(variable.build())
            }
            builder.addType(errorsBuilder.build())
        }
    }

    private class EscalationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ESCALATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.escalations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnEscalationClass = ClassName("${modelApi.packagePath}.types", "BpmnEscalation")
            val escalationsBuilder = TypeSpec.objectBuilder("Escalations")
            modelApi.model.escalations.forEach {
                val (escalationName, escalationCode) = it.getValue()
                val instanceBuilder = PropertySpec.builder(it.getName(), bpmnEscalationClass)
                val variable = instanceBuilder.initializer("BpmnEscalation(\"$escalationName\", \"$escalationCode\")")
                escalationsBuilder.addProperty(variable.build())
            }
            builder.addType(escalationsBuilder.build())
        }
    }

    private inner class CompensationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.COMPENSATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.compensations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val elementIdClass = ClassName("${modelApi.packagePath}.types", "ElementId")
            val compensationsBuilder = TypeSpec.objectBuilder("Compensations")
            modelApi.model.compensations.forEach { compensation ->
                compensationsBuilder.addProperty(createTypedAttribute(compensation, elementIdClass))
            }
            builder.addType(compensationsBuilder.build())
        }
    }

    private inner class TimersWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnTimerClass = ClassName("${modelApi.packagePath}.types", "BpmnTimer")
            val timersBuilder = TypeSpec.objectBuilder("Timers")
            modelApi.model.timers.forEach { timer ->
                val (timerType, timerValue) = timer.getValue()
                val cleanTimerValue = timerValue.escapeDollarInterpolation()
                val instanceBuilder = PropertySpec.builder(timer.getName(), bpmnTimerClass)
                val variable = instanceBuilder.initializer("BpmnTimer(\"$timerType\", \"$cleanTimerValue\")")
                timersBuilder.addProperty(variable.build())
            }
            builder.addType(timersBuilder.build())
        }
    }

    private fun createAttribute(variable: VariableMapping<String>): PropertySpec {
        val cleanValue = variable.getValue().escapeDollarInterpolation()
        return PropertySpec.builder(variable.getName(), String::class)
            .addModifiers(KModifier.CONST)
            .initializer("\"$cleanValue\"")
            .build()
    }

    private fun createTypedAttribute(variable: VariableMapping<String>, wrapperClass: ClassName): PropertySpec {
        val cleanValue = variable.getValue().escapeDollarInterpolation()
        return PropertySpec.builder(variable.getName(), wrapperClass)
            .initializer("${wrapperClass.simpleName}(\"$cleanValue\")")
            .build()
    }

    private fun String.escapeDollarInterpolation(): String {
        // Prevent KotlinPoet from interpreting "${...}" in string literals as interpolation
        return this.replace("\${", "\\\${")
    }
}
