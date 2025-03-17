package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.palantir.javapoet.*
import io.github.emaarco.bpmn.adapter.outbound.codegen.WriteApiFileAdapter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import javax.lang.model.element.Modifier.*

class JavaApiBuilder : WriteApiFileAdapter.AbstractApiBuilder<TypeSpec.Builder>() {

    override fun buildApiFile(modelApi: BpmnModelApi) = with(modelApi.model) {

        val className = modelApi.fileName()
        val rootClassBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL)

        writeProcessId(rootClassBuilder, modelApi.model)
        writeElements(rootClassBuilder, modelApi.model)
        writeMessages(rootClassBuilder, modelApi.model)
        writeServiceTasks(rootClassBuilder, modelApi.model)

        if (timers.isNotEmpty()) writeTimers(rootClassBuilder, modelApi.model)
        if (errors.isNotEmpty()) writeErrors(rootClassBuilder, modelApi.model)
        if (signals.isNotEmpty()) writeSignals(rootClassBuilder, modelApi.model)

        val fileBuilder = JavaFile.builder(modelApi.packagePath, rootClassBuilder.build())
        val javaFile = fileBuilder.addFileComment(autoGenComment).build()
        javaFile.writeTo(modelApi.outputFolder)

        println("Generated: ${modelApi.fileName()}.java")
    }

    override fun writeProcessId(builder: TypeSpec.Builder, model: BpmnModel) {
        val fieldBuilder = FieldSpec.builder(String::class.java, "PROCESS_ID").addModifiers(PUBLIC, FINAL, STATIC)
        builder.addField(fieldBuilder.initializer("\$S", model.processId).build())
    }

    override fun writeElements(builder: TypeSpec.Builder, model: BpmnModel) {
        val elementsBuilder = TypeSpec.classBuilder("Elements").addModifiers(PUBLIC, STATIC, FINAL)
        model.flowNodes.forEach { flowNode -> elementsBuilder.addField(createAttribute(flowNode)) }
        builder.addType(elementsBuilder.build())
    }

    override fun writeMessages(builder: TypeSpec.Builder, model: BpmnModel) {
        val messagesBuilder = TypeSpec.classBuilder("Messages").addModifiers(PUBLIC, STATIC, FINAL)
        model.messages.forEach { message -> messagesBuilder.addField(createAttribute(message)) }
        builder.addType(messagesBuilder.build())
    }

    override fun writeServiceTasks(builder: TypeSpec.Builder, model: BpmnModel) {
        val tasksBuilder = TypeSpec.classBuilder("TaskTypes").addModifiers(PUBLIC, STATIC, FINAL)
        model.serviceTasks.forEach { task -> tasksBuilder.addField(createAttribute(task)) }
        builder.addType(tasksBuilder.build())
    }

    override fun writeTimers(builder: TypeSpec.Builder, model: BpmnModel) {
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

    override fun writeErrors(builder: TypeSpec.Builder, model: BpmnModel) {
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

    override fun writeSignals(builder: TypeSpec.Builder, model: BpmnModel) {
        val signalsBuilder = TypeSpec.classBuilder("Signals").addModifiers(PUBLIC, STATIC, FINAL)
        model.signals.forEach { signal -> signalsBuilder.addField(createAttribute(signal)) }
        builder.addType(signalsBuilder.build())
    }

    private fun createAttribute(variable: VariableMapping<*>): FieldSpec {
        return FieldSpec.builder(String::class.java, variable.getName())
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer("\$S", variable.getValue())
            .build()
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
