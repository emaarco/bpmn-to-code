package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.palantir.javapoet.ClassName
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.WriteApiFileAdapter
import io.github.emaarco.bpmn.adapter.outbound.codegen.writer.ObjectWriter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import javax.lang.model.element.Modifier.*

class JavaApiBuilder : WriteApiFileAdapter.AbstractApiBuilder<TypeSpec.Builder>() {

    private val objectWriters: Map<ApiObjectType, ObjectWriter<TypeSpec.Builder>> = mapOf(
        ApiObjectType.PROCESS_ID to ProcessIdWriter(),
        ApiObjectType.ELEMENTS to ElementsWriter(),
        ApiObjectType.MESSAGES to MessagesWriter(),
        ApiObjectType.SERVICE_TASKS to ServiceTasksWriter(),
        ApiObjectType.TIMERS to TimersWriter(),
        ApiObjectType.ERRORS to ErrorsWriter(),
        ApiObjectType.SIGNALS to SignalsWriter()
    )

    override fun buildApiFile(modelApi: BpmnModelApi) {
        val className = modelApi.fileName()
        val rootClassBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL)

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi.model) }
        relevantWriters.forEach { (_, writer) -> writer.write(rootClassBuilder, modelApi.model) }

        val fileBuilder = JavaFile.builder(modelApi.packagePath, rootClassBuilder.build())
        val javaFile = fileBuilder.addFileComment(autoGenComment).build()
        javaFile.writeTo(modelApi.outputFolder)

        println("Generated: ${modelApi.fileName()}.java")
    }

    private class ProcessIdWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(model: BpmnModel) = true

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val fieldBuilder = FieldSpec.builder(String::class.java, "PROCESS_ID").addModifiers(PUBLIC, FINAL, STATIC)
            builder.addField(fieldBuilder.initializer("\$S", model.processId).build())
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(model: BpmnModel) = true

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val elementsBuilder = TypeSpec.classBuilder("Elements").addModifiers(PUBLIC, STATIC, FINAL)
            model.flowNodes.forEach { flowNode -> elementsBuilder.addField(createAttribute(flowNode)) }
            builder.addType(elementsBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(model: BpmnModel) = model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val messagesBuilder = TypeSpec.classBuilder("Messages").addModifiers(PUBLIC, STATIC, FINAL)
            model.messages.forEach { message -> messagesBuilder.addField(createAttribute(message)) }
            builder.addType(messagesBuilder.build())
        }
    }

    private inner class ServiceTasksWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(model: BpmnModel) = model.serviceTasks.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val tasksBuilder = TypeSpec.classBuilder("TaskTypes").addModifiers(PUBLIC, STATIC, FINAL)
            model.serviceTasks.forEach { task -> tasksBuilder.addField(createAttribute(task)) }
            builder.addType(tasksBuilder.build())
        }
    }

    private inner class SignalsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(model: BpmnModel) = model.signals.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val signalsBuilder = TypeSpec.classBuilder("Signals").addModifiers(PUBLIC, STATIC, FINAL)
            model.signals.forEach { signal -> signalsBuilder.addField(createAttribute(signal)) }
            builder.addType(signalsBuilder.build())
        }
    }

    private class ErrorsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(model: BpmnModel): Boolean = model.errors.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val errorsBuilder = TypeSpec.classBuilder("Errors").addModifiers(PUBLIC, STATIC, FINAL)
            errorsBuilder.addType(buildErrorClass())
            model.errors.forEach {
                val (errorName, errorCode) = it.getValue()
                val instanceBuilder = FieldSpec.builder(ClassName.get("", "BpmnError"), it.getName())
                val variable = instanceBuilder.addModifiers(PUBLIC, STATIC, FINAL)
                errorsBuilder.addField(variable.initializer("new BpmnError(\$S, \$S)", errorName, errorCode).build())
            }
            builder.addType(errorsBuilder.build())
        }

        private fun buildErrorClass(): TypeSpec {
            val builder = TypeSpec.classBuilder("BpmnError").addModifiers(PUBLIC, STATIC)
            builder.addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "code").addModifiers(PUBLIC, FINAL).build())
            builder.addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(String::class.java, "name")
                    .addParameter(String::class.java, "code")
                    .addStatement("this.name = name")
                    .addStatement("this.code = code")
                    .build()
            )
            return builder.build()
        }
    }

    private class TimersWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(model: BpmnModel): Boolean = model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, model: BpmnModel) {
            val timersBuilder = TypeSpec.classBuilder("Timers").addModifiers(PUBLIC, STATIC, FINAL)
            timersBuilder.addType(buildTimerClass())
            model.timers.forEach {
                val (timerType, timerValue) = it.getValue()
                val instanceBuilder = FieldSpec.builder(ClassName.get("", "BpmnTimer"), it.getName())
                val variable = instanceBuilder.addModifiers(PUBLIC, STATIC, FINAL)
                timersBuilder.addField(variable.initializer("new BpmnTimer(\$S, \$S)", timerType, timerValue).build())
            }
            builder.addType(timersBuilder.build())
        }

        private fun buildTimerClass(): TypeSpec {
            val builder = TypeSpec.classBuilder("BpmnTimer").addModifiers(PUBLIC, STATIC)
            builder.addField(FieldSpec.builder(String::class.java, "type").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "timerValue").addModifiers(PUBLIC, FINAL).build())
            builder.addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(String::class.java, "type")
                    .addParameter(String::class.java, "timerValue")
                    .addStatement("this.type = type")
                    .addStatement("this.timerValue = timerValue")
                    .build()
            )
            return builder.build()
        }
    }

    private fun createAttribute(variable: VariableMapping<*>): FieldSpec {
        return FieldSpec.builder(String::class.java, variable.getName())
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer("\$S", variable.getValue())
            .build()
    }
}