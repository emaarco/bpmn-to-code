package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.squareup.kotlinpoet.*
import io.github.emaarco.bpmn.adapter.outbound.codegen.WriteApiFileAdapter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import java.io.File

class KotlinApiBuilder : WriteApiFileAdapter.AbstractApiBuilder<TypeSpec.Builder>() {

    override fun buildApiFile(modelApi: BpmnModelApi) = with(modelApi.model) {

        val objectName = modelApi.fileName()
        val unusedAnnotation = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()
        val rootObjectBuilder = TypeSpec.objectBuilder(objectName).addAnnotation(unusedAnnotation)
        val fileSpecBuilder = FileSpec.builder(modelApi.packagePath, objectName).addFileComment(autoGenComment)

        writeProcessId(rootObjectBuilder, modelApi.model)
        writeElements(rootObjectBuilder, modelApi.model)
        writeMessages(rootObjectBuilder, modelApi.model)
        writeServiceTasks(rootObjectBuilder, modelApi.model)

        if (timers.isNotEmpty()) writeTimers(rootObjectBuilder, modelApi.model)
        if (errors.isNotEmpty()) writeErrors(rootObjectBuilder, modelApi.model)
        if (signals.isNotEmpty()) writeSignals(rootObjectBuilder, modelApi.model)

        fileSpecBuilder.addType(rootObjectBuilder.build()).addAnnotation(unusedAnnotation)
        val fileSpec = fileSpecBuilder.build()
        val file = fileSpec.writeTo(modelApi.outputFolder)
        file.removeUnnecessaryPublicModifier()

        println("Generated: $objectName.kt")
    }

    override fun writeProcessId(builder: TypeSpec.Builder, model: BpmnModel) {
        val idPropertyBuilder = PropertySpec.builder("PROCESS_ID", String::class)
        val idProperty = idPropertyBuilder.initializer("\"${model.processId}\"").build()
        builder.addProperty(idProperty)
    }

    override fun writeElements(builder: TypeSpec.Builder, model: BpmnModel) {
        val elementsBuilder = TypeSpec.objectBuilder("Elements")
        model.flowNodes.forEach { flowNode -> elementsBuilder.addProperty(createAttribute(flowNode)) }
        builder.addType(elementsBuilder.build())
    }

    override fun writeMessages(builder: TypeSpec.Builder, model: BpmnModel) {
        val messagesBuilder = TypeSpec.objectBuilder("Messages")
        model.messages.forEach { message -> messagesBuilder.addProperty(createAttribute(message)) }
        builder.addType(messagesBuilder.build())
    }

    override fun writeServiceTasks(builder: TypeSpec.Builder, model: BpmnModel) {
        val tasksBuilder = TypeSpec.objectBuilder("TaskTypes")
        model.serviceTasks.forEach { task -> tasksBuilder.addProperty(createAttribute(task)) }
        builder.addType(tasksBuilder.build())
    }

    override fun writeSignals(builder: TypeSpec.Builder, model: BpmnModel) {
        val signalsBuilder = TypeSpec.objectBuilder("Signals")
        model.signals.forEach { signal -> signalsBuilder.addProperty(createAttribute(signal)) }
        builder.addType(signalsBuilder.build())
    }

    override fun writeErrors(builder: TypeSpec.Builder, model: BpmnModel) {
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

    override fun writeTimers(builder: TypeSpec.Builder, model: BpmnModel) {
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

    private fun createAttribute(variable: VariableMapping<String>): PropertySpec {
        val cleanValue = variable.getValue().escapeDollarInterpolation()
        return PropertySpec.builder(variable.getName(), String::class)
            .addModifiers(KModifier.PUBLIC)
            .initializer("\"$cleanValue\"")
            .build()
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

    private fun buildErrorDataClass(): TypeSpec {
        val constructor = FunSpec.constructorBuilder().addStringParameter("name").addStringParameter("code").build()
        return TypeSpec.classBuilder("BpmnError")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(constructor)
            .addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
            .addProperty(PropertySpec.builder("code", STRING).initializer("code").build())
            .build()
    }

    private fun File.removeUnnecessaryPublicModifier() {
        val text = this.readText().replace("public ", "")
        this.writeText(text)
    }

    private fun FunSpec.Builder.addStringParameter(name: String) = addParameter(name, String::class)

    /**
     * Some process configurations can reference to variables, by using `${variableName}`.
     * This method escapes the dollar sign to prevent Kotlin from interpreting it as an interpolation.
     */
    private fun String.escapeDollarInterpolation(): String {
        return this.replace("\${", "\\\${")
    }

}
