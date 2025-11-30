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
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class KotlinApiBuilder : CodeGenerationAdapter.AbstractApiBuilder<TypeSpec.Builder>() {

    private val objectWriters: Map<ApiObjectType, ObjectWriter<TypeSpec.Builder>> = mapOf(
        ApiObjectType.PROCESS_ID to ProcessIdWriter(),
        ApiObjectType.ELEMENTS to ElementsWriter(),
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

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi.model) }
        relevantWriters.forEach { (_, writer) -> writer.write(rootObjectBuilder, modelApi.model) }

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
        override fun shouldWrite(model: BpmnModel) = true

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val idPropertyBuilder = PropertySpec.builder("PROCESS_ID", String::class).addModifiers(KModifier.CONST)
            val idProperty = idPropertyBuilder.initializer("\"${model.processId}\"").build()
            builder.addProperty(idProperty)
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(model: BpmnModel) = true

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val elementsBuilder = TypeSpec.objectBuilder("Elements")
            model.flowNodes.forEach { flowNode -> elementsBuilder.addProperty(createAttribute(flowNode)) }
            builder.addType(elementsBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(model: BpmnModel) = model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val messagesBuilder = TypeSpec.objectBuilder("Messages")
            model.messages.forEach { message -> messagesBuilder.addProperty(createAttribute(message)) }
            builder.addType(messagesBuilder.build())
        }
    }

    private inner class ServiceTasksWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(model: BpmnModel) = model.serviceTasks.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val tasksBuilder = TypeSpec.objectBuilder("TaskTypes")
            model.serviceTasks.forEach { task -> tasksBuilder.addProperty(createAttribute(task)) }
            builder.addType(tasksBuilder.build())
        }
    }

    private inner class SignalsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(model: BpmnModel) = model.signals.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val signalsBuilder = TypeSpec.objectBuilder("Signals")
            model.signals.forEach { signal -> signalsBuilder.addProperty(createAttribute(signal)) }
            builder.addType(signalsBuilder.build())
        }
    }

    private inner class VariablesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(model: BpmnModel) = model.variables.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val variablesBuilder = TypeSpec.objectBuilder("Variables")
            model.variables.forEach { variable -> variablesBuilder.addProperty(createAttribute(variable)) }
            builder.addType(variablesBuilder.build())
        }
    }

    private class ErrorsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(model: BpmnModel): Boolean = model.errors.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val errorsBuilder = TypeSpec.objectBuilder("Errors")
            errorsBuilder.addType(buildErrorDataClass())
            model.errors.forEach {
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
        override fun shouldWrite(model: BpmnModel): Boolean = model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val timersBuilder = TypeSpec.objectBuilder("Timers")
            timersBuilder.addType(buildTimerDataClass())
            model.timers.forEach { timer ->
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

    private fun createAttribute(variable: VariableMapping<String>): PropertySpec {
        val cleanValue = variable.getValue().escapeDollarInterpolation()
        return PropertySpec.builder(variable.getName(), String::class)
            .addModifiers(KModifier.CONST)
            .initializer("\"$cleanValue\"")
            .build()
    }

    private fun String.escapeDollarInterpolation(): String {
        return this.replace("\${", "\\\${")
    }
}
