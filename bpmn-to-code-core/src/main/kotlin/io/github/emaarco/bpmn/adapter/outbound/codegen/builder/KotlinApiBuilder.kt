package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.codegen.writer.ObjectWriter
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.BpmnElementType
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
        ApiObjectType.SIGNALS to SignalsWriter(),
        ApiObjectType.VARIABLES to VariablesWriter()
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
            elementGroups.forEach { (groupName, types) ->
                val groupNodes = modelApi.model.flowNodes
                    .filter { it.elementType in types }
                    .sortedBy { it.getName() }
                if (groupNodes.isNotEmpty()) {
                    val groupBuilder = TypeSpec.objectBuilder(groupName)
                    groupNodes.forEach { groupBuilder.addProperty(createAttribute(it)) }
                    elementsBuilder.addType(groupBuilder.build())
                }
            }
            builder.addType(elementsBuilder.build())
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
            val errorsBuilder = TypeSpec.objectBuilder("Errors")
            errorsBuilder.addType(buildErrorDataClass())
            modelApi.model.errors.forEach {
                val (errorName, errorCode) = it.getValue()
                val instanceBuilder = PropertySpec.builder(it.getName(), ClassName("", "BpmnError"))
                val variable = instanceBuilder.initializer("BpmnError(\"$errorName\", \"$errorCode\")")
                errorsBuilder.addProperty(variable.build())
            }
            builder.addType(errorsBuilder.build())
        }

        private fun buildErrorDataClass(): TypeSpec {
            val constructor = FunSpec.constructorBuilder().addStringParameter("name").addStringParameter("code").build()
            return TypeSpec.classBuilder("BpmnError")
                .addModifiers(KModifier.DATA)
                .primaryConstructor(constructor)
                .addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
                .addProperty(PropertySpec.builder("code", STRING).initializer("code").build())
                .build()
        }

        private fun FunSpec.Builder.addStringParameter(name: String) = addParameter(name, String::class)
    }

    private inner class TimersWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val timersBuilder = TypeSpec.objectBuilder("Timers")
            timersBuilder.addType(buildTimerDataClass())
            modelApi.model.timers.forEach { timer ->
                val (timerType, timerValue) = timer.getValue()
                val cleanTimerValue = timerValue.escapeDollarInterpolation()
                val instanceBuilder = PropertySpec.builder(timer.getName(), ClassName("", "BpmnTimer"))
                val variable = instanceBuilder.initializer("BpmnTimer(\"$timerType\", \"$cleanTimerValue\")")
                timersBuilder.addProperty(variable.build())
            }
            builder.addType(timersBuilder.build())
        }

        private fun buildTimerDataClass(): TypeSpec {
            val constructor = FunSpec.constructorBuilder().addStringParameter("type").addStringParameter("timerValue")
            return TypeSpec.classBuilder("BpmnTimer")
                .addModifiers(KModifier.DATA)
                .primaryConstructor(constructor.build())
                .addProperty(PropertySpec.builder("type", String::class).initializer("type").build())
                .addProperty(PropertySpec.builder("timerValue", String::class).initializer("timerValue").build())
                .build()
        }

        private fun FunSpec.Builder.addStringParameter(name: String) = addParameter(name, String::class)
    }

    companion object {
        private val elementGroups = linkedMapOf(
            "Tasks" to setOf(
                BpmnElementType.SERVICE_TASK,
                BpmnElementType.USER_TASK,
                BpmnElementType.RECEIVE_TASK,
                BpmnElementType.SEND_TASK,
                BpmnElementType.SCRIPT_TASK,
                BpmnElementType.MANUAL_TASK,
                BpmnElementType.BUSINESS_RULE_TASK,
            ),
            "Events" to setOf(
                BpmnElementType.START_EVENT,
                BpmnElementType.END_EVENT,
                BpmnElementType.INTERMEDIATE_CATCH_EVENT,
                BpmnElementType.INTERMEDIATE_THROW_EVENT,
                BpmnElementType.BOUNDARY_EVENT,
            ),
            "Gateways" to setOf(
                BpmnElementType.EXCLUSIVE_GATEWAY,
                BpmnElementType.PARALLEL_GATEWAY,
                BpmnElementType.INCLUSIVE_GATEWAY,
                BpmnElementType.EVENT_BASED_GATEWAY,
                BpmnElementType.COMPLEX_GATEWAY,
            ),
            "Containers" to setOf(
                BpmnElementType.SUB_PROCESS,
                BpmnElementType.TRANSACTION,
            ),
            "CallActivities" to setOf(
                BpmnElementType.CALL_ACTIVITY,
            ),
        )
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
