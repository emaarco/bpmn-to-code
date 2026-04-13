package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.codegen.writer.ObjectWriter
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase

class KotlinApiBuilder : CodeGenerationAdapter.AbstractApiBuilder<TypeSpec.Builder>() {

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
    )

    override fun buildApiFile(modelApi: BpmnModelApi): List<GeneratedApiFile> {
        val processApiFile = buildProcessApiFile(modelApi)
        val typeFiles = buildSharedTypeFiles(modelApi.packagePath, modelApi.outputLanguage)
        return listOf(processApiFile) + typeFiles
    }

    private fun buildProcessApiFile(modelApi: BpmnModelApi): GeneratedApiFile {
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

    private fun buildSharedTypeFiles(packagePath: String, language: OutputLanguage): List<GeneratedApiFile> {
        val typesPackage = "$packagePath.types"
        return listOf(
            buildBpmnTimerFile(typesPackage, language),
            buildBpmnErrorFile(typesPackage, language),
            buildBpmnEscalationFile(typesPackage, language),
            buildBpmnFlowFile(typesPackage, language),
            buildBpmnRelationsFile(typesPackage, language),
        )
    }

    private fun buildBpmnTimerFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnTimer")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("type", STRING)
                    .addParameter("timerValue", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("type", STRING).initializer("type").build())
            .addProperty(PropertySpec.builder("timerValue", STRING).initializer("timerValue").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnTimer", typeSpec, language)
    }

    private fun buildBpmnErrorFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnError")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", STRING)
                    .addParameter("code", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
            .addProperty(PropertySpec.builder("code", STRING).initializer("code").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnError", typeSpec, language)
    }

    private fun buildBpmnEscalationFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnEscalation")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", STRING)
                    .addParameter("code", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
            .addProperty(PropertySpec.builder("code", STRING).initializer("code").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnEscalation", typeSpec, language)
    }

    private fun buildBpmnFlowFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val nullableString = STRING.copy(nullable = true)
        val typeSpec = TypeSpec.classBuilder("BpmnFlow")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("id", STRING)
                    .addParameter("sourceRef", STRING)
                    .addParameter("targetRef", STRING)
                    .addParameter(ParameterSpec.builder("condition", nullableString).defaultValue("null").build())
                    .addParameter(ParameterSpec.builder("isDefault", BOOLEAN).defaultValue("false").build())
                    .build()
            )
            .addProperty(PropertySpec.builder("id", STRING).initializer("id").build())
            .addProperty(PropertySpec.builder("sourceRef", STRING).initializer("sourceRef").build())
            .addProperty(PropertySpec.builder("targetRef", STRING).initializer("targetRef").build())
            .addProperty(PropertySpec.builder("condition", nullableString).initializer("condition").build())
            .addProperty(PropertySpec.builder("isDefault", BOOLEAN).initializer("isDefault").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnFlow", typeSpec, language)
    }

    private fun buildBpmnRelationsFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val listType = LIST.parameterizedBy(STRING)
        val nullableString = STRING.copy(nullable = true)
        val typeSpec = TypeSpec.classBuilder("BpmnRelations")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("incoming", listType)
                    .addParameter("outgoing", listType)
                    .addParameter("parentId", nullableString)
                    .addParameter("attachedToRef", nullableString)
                    .addParameter("attachedElements", listType)
                    .build()
            )
            .addProperty(PropertySpec.builder("incoming", listType).initializer("incoming").build())
            .addProperty(PropertySpec.builder("outgoing", listType).initializer("outgoing").build())
            .addProperty(PropertySpec.builder("parentId", nullableString).initializer("parentId").build())
            .addProperty(PropertySpec.builder("attachedToRef", nullableString).initializer("attachedToRef").build())
            .addProperty(PropertySpec.builder("attachedElements", listType).initializer("attachedElements").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnRelations", typeSpec, language)
    }

    private fun buildTypeFile(typesPackage: String, className: String, typeSpec: TypeSpec, language: OutputLanguage): GeneratedApiFile {
        val fileSpec = FileSpec.builder(typesPackage, className)
            .addFileComment(autoGenComment)
            .addType(typeSpec)
            .build()
        val content = buildString { fileSpec.writeTo(this) }.replace("public ", "")
        return GeneratedApiFile(
            fileName = "$className.kt",
            packagePath = typesPackage,
            content = content,
            language = language,
        )
    }

    private class ProcessIdWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val idPropertyBuilder = PropertySpec.builder("PROCESS_ID", String::class).addModifiers(KModifier.CONST)
            val idProperty = idPropertyBuilder.initializer("\"${modelApi.model.processId}\"").build()
            builder.addProperty(idProperty)
        }
    }

    private class ProcessEngineWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val enginePropertyBuilder = PropertySpec.builder("PROCESS_ENGINE", String::class).addModifiers(KModifier.CONST)
            val engineProperty = enginePropertyBuilder.initializer("\"${modelApi.engine.name}\"").build()
            builder.addProperty(engineProperty)
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val elementsBuilder = TypeSpec.objectBuilder("Elements")
            modelApi.model.flowNodes.forEach { flowNode -> elementsBuilder.addProperty(createAttribute(flowNode)) }
            builder.addType(elementsBuilder.build())
        }
    }

    private inner class FlowsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.FLOWS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.sequenceFlows.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnFlowClass = ClassName("${modelApi.packagePath}.types", "BpmnFlow")
            val flowsBuilder = TypeSpec.objectBuilder("Flows")
            modelApi.model.sequenceFlows.forEach { flow ->
                val initStr = buildFlowInitializer(flow.id ?: "", flow.sourceRef, flow.targetRef, flow.conditionExpression, flow.isDefault)
                flowsBuilder.addProperty(PropertySpec.builder(flow.getName(), bpmnFlowClass).initializer(initStr).build())
            }
            builder.addType(flowsBuilder.build())
        }

        private fun buildFlowInitializer(id: String, sourceRef: String, targetRef: String, condition: String?, isDefault: Boolean): String {
            return buildString {
                append("BpmnFlow(\n")
                append("    id = \"$id\",\n")
                append("    sourceRef = \"$sourceRef\",\n")
                append("    targetRef = \"$targetRef\",\n")
                if (condition != null) append("    condition = \"${condition.escapeDollarInterpolation()}\",\n")
                if (isDefault) append("    isDefault = true,\n")
                append(")")
            }
        }
    }

    private inner class RelationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.sequenceFlows.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnRelationsClass = ClassName("${modelApi.packagePath}.types", "BpmnRelations")
            val relationsBuilder = TypeSpec.objectBuilder("Relations")
            modelApi.model.flowNodes
                .filter { it.id != null }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val initStr = buildRelationsInitializer(node)
                    relationsBuilder.addProperty(PropertySpec.builder(node.getName(), bpmnRelationsClass).initializer(initStr).build())
                }
            builder.addType(relationsBuilder.build())
        }

        private fun buildRelationsInitializer(node: FlowNodeDefinition): String {
            return buildString {
                append("BpmnRelations(\n")
                append("    incoming = ${listLiteral(node.incoming)},\n")
                append("    outgoing = ${listLiteral(node.outgoing)},\n")
                append("    parentId = ${nullableStringLiteral(node.parentId)},\n")
                append("    attachedToRef = ${nullableStringLiteral(node.attachedToRef)},\n")
                append("    attachedElements = ${listLiteral(node.attachedElements)},\n")
                append(")")
            }
        }

        private fun listLiteral(items: List<String>): String {
            return if (items.isEmpty()) "emptyList()" else "listOf(${items.joinToString { "\"$it\"" }})"
        }

        private fun nullableStringLiteral(value: String?): String {
            return if (value != null) "\"$value\"" else "null"
        }
    }

    private inner class CallActivitiesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.CALL_ACTIVITIES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.callActivities.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val callActivitiesBuilder = TypeSpec.objectBuilder("CallActivities")
            modelApi.model.callActivities.forEach { callActivity -> callActivitiesBuilder.addProperty(createAttribute(callActivity)) }
            builder.addType(callActivitiesBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val messagesBuilder = TypeSpec.objectBuilder("Messages")
            modelApi.model.messages.forEach { message -> messagesBuilder.addProperty(createAttribute(message)) }
            builder.addType(messagesBuilder.build())
        }
    }

    private inner class ServiceTasksWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.serviceTasks.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val tasksBuilder = TypeSpec.objectBuilder("TaskTypes")
            modelApi.model.serviceTasks.forEach { task -> tasksBuilder.addProperty(createAttribute(task)) }
            builder.addType(tasksBuilder.build())
        }
    }

    private inner class SignalsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.signals.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val signalsBuilder = TypeSpec.objectBuilder("Signals")
            modelApi.model.signals.forEach { signal -> signalsBuilder.addProperty(createAttribute(signal)) }
            builder.addType(signalsBuilder.build())
        }
    }

    private inner class VariablesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val variablesBuilder = TypeSpec.objectBuilder("Variables")
            modelApi.model.variables.forEach { variable -> variablesBuilder.addProperty(createAttribute(variable)) }
            modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val objectName = (node.getRawName()).toCamelCase()
                    val nodeVarsBuilder = TypeSpec.objectBuilder(objectName)
                    val sortedVariables = node.variables.sortedBy { it.getRawName() }
                    sortedVariables.forEach { nodeVarsBuilder.addProperty(createAttribute(it)) }
                    variablesBuilder.addType(nodeVarsBuilder.build())
                }
            builder.addType(variablesBuilder.build())
        }
    }

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
            val compensationsBuilder = TypeSpec.objectBuilder("Compensations")
            modelApi.model.compensations.forEach { compensation ->
                compensationsBuilder.addProperty(createAttribute(compensation))
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

    private fun String.escapeDollarInterpolation(): String {
        // Prevent KotlinPoet from interpreting "${...}" in string literals as interpolation
        return this.replace("\${", "\\\${")
    }
}
