package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.palantir.javapoet.ClassName
import com.palantir.javapoet.CodeBlock
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.ParameterizedTypeName
import com.palantir.javapoet.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.codegen.writer.ObjectWriter
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.ApiObjectType
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase
import javax.lang.model.element.Modifier.*

class JavaApiBuilder : CodeGenerationAdapter.AbstractApiBuilder<TypeSpec.Builder>() {

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
        ApiObjectType.SIGNALS to SignalsWriter(),
        ApiObjectType.VARIABLES to VariablesWriter(),
        ApiObjectType.FLOWS to FlowsWriter(),
        ApiObjectType.RELATIONS to RelationsWriter(),
    )

    override fun buildApiFile(modelApi: BpmnModelApi): GeneratedApiFile {

        val className = modelApi.fileName()
        val rootClassBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL)

        val relevantWriters = objectWriters.filter { it.value.shouldWrite(modelApi) }
        relevantWriters.forEach { (_, writer) -> writer.write(rootClassBuilder, modelApi) }

        val fileBuilder = JavaFile.builder(modelApi.packagePath, rootClassBuilder.build())
        val javaFile = fileBuilder.addFileComment(autoGenComment).build()

        val fileContent = buildString { javaFile.writeTo(this) }

        return GeneratedApiFile(
            fileName = "${modelApi.fileName()}.java",
            packagePath = modelApi.packagePath,
            content = fileContent,
            language = modelApi.outputLanguage
        )
    }

    private class ProcessIdWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ID
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val fieldBuilder = FieldSpec.builder(String::class.java, "PROCESS_ID").addModifiers(PUBLIC, FINAL, STATIC)
            builder.addField(fieldBuilder.initializer("\$S", modelApi.model.processId).build())
        }
    }

    private class ProcessEngineWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val fieldBuilder = FieldSpec.builder(String::class.java, "PROCESS_ENGINE").addModifiers(PUBLIC, FINAL, STATIC)
            builder.addField(fieldBuilder.initializer("\$S", modelApi.engine.name).build())
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val elementsBuilder = TypeSpec.classBuilder("Elements").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.flowNodes.forEach { flowNode -> elementsBuilder.addField(createAttribute(flowNode)) }
            builder.addType(elementsBuilder.build())
        }
    }

    private inner class FlowsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.FLOWS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.sequenceFlows.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val flowsBuilder = TypeSpec.classBuilder("Flows").addModifiers(PUBLIC, STATIC, FINAL)
            flowsBuilder.addType(buildFlowClass())
            modelApi.model.sequenceFlows.forEach { flow ->
                val initCode = buildFlowInitializer(flow.id ?: "", flow.sourceRef, flow.targetRef, flow.conditionExpression, flow.isDefault)
                val fieldBuilder = FieldSpec.builder(ClassName.get("", "BpmnFlow"), flow.getName()).addModifiers(PUBLIC, STATIC, FINAL)
                flowsBuilder.addField(fieldBuilder.initializer(initCode).build())
            }
            builder.addType(flowsBuilder.build())
        }

        private fun buildFlowInitializer(id: String, sourceRef: String, targetRef: String, condition: String?, isDefault: Boolean): CodeBlock {
            val conditionBlock = if (condition != null) CodeBlock.of("\$S", condition) else CodeBlock.of("null")
            return CodeBlock.builder()
                .add("new BpmnFlow(\$S, \$S, \$S, ", id, sourceRef, targetRef)
                .add(conditionBlock)
                .add(", \$L)", isDefault)
                .build()
        }

        private fun buildFlowClass(): TypeSpec {
            val builder = TypeSpec.classBuilder("BpmnFlow").addModifiers(PUBLIC, STATIC)
            builder.addField(FieldSpec.builder(String::class.java, "id").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "sourceRef").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "targetRef").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "condition").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(Boolean::class.javaPrimitiveType!!, "isDefault").addModifiers(PUBLIC, FINAL).build())
            builder.addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(String::class.java, "id")
                    .addParameter(String::class.java, "sourceRef")
                    .addParameter(String::class.java, "targetRef")
                    .addParameter(String::class.java, "condition")
                    .addParameter(Boolean::class.javaPrimitiveType!!, "isDefault")
                    .addStatement("this.id = id")
                    .addStatement("this.sourceRef = sourceRef")
                    .addStatement("this.targetRef = targetRef")
                    .addStatement("this.condition = condition")
                    .addStatement("this.isDefault = isDefault")
                    .build()
            )
            return builder.build()
        }
    }

    private inner class RelationsWriter : ObjectWriter<TypeSpec.Builder> {

        private val listClass = ClassName.get("java.util", "List")
        private val listType = ParameterizedTypeName.get(listClass, ClassName.get("java.lang", "String"))

        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.sequenceFlows.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val relationsBuilder = TypeSpec.classBuilder("Relations").addModifiers(PUBLIC, STATIC, FINAL)
            relationsBuilder.addType(buildRelationsClass())
            modelApi.model.flowNodes
                .filter { it.id != null }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val initCode = buildRelationsInitializer(node)
                    val fieldBuilder = FieldSpec.builder(ClassName.get("", "BpmnRelations"), node.getName()).addModifiers(PUBLIC, STATIC, FINAL)
                    relationsBuilder.addField(fieldBuilder.initializer(initCode).build())
                }
            builder.addType(relationsBuilder.build())
        }

        private fun buildRelationsInitializer(node: FlowNodeDefinition): CodeBlock {
            val parentIdBlock = if (node.parentId != null) CodeBlock.of("\$S", node.parentId) else CodeBlock.of("null")
            val attachedToRefBlock = if (node.attachedToRef != null) CodeBlock.of("\$S", node.attachedToRef) else CodeBlock.of("null")
            return CodeBlock.builder()
                .add("new BpmnRelations(")
                .add(javaListLiteral(node.incoming))
                .add(", ")
                .add(javaListLiteral(node.outgoing))
                .add(", ")
                .add(parentIdBlock)
                .add(", ")
                .add(attachedToRefBlock)
                .add(", ")
                .add(javaListLiteral(node.attachedElements))
                .add(")")
                .build()
        }

        private fun javaListLiteral(items: List<String>): CodeBlock {
            if (items.isEmpty()) return CodeBlock.of("\$T.of()", listClass)
            val builder = CodeBlock.builder().add("\$T.of(", listClass)
            items.forEachIndexed { i, item ->
                if (i > 0) builder.add(", ")
                builder.add("\$S", item)
            }
            builder.add(")")
            return builder.build()
        }

        private fun buildRelationsClass(): TypeSpec {
            val builder = TypeSpec.classBuilder("BpmnRelations").addModifiers(PUBLIC, STATIC)
            builder.addField(FieldSpec.builder(listType, "incoming").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(listType, "outgoing").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "parentId").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(String::class.java, "attachedToRef").addModifiers(PUBLIC, FINAL).build())
            builder.addField(FieldSpec.builder(listType, "attachedElements").addModifiers(PUBLIC, FINAL).build())
            builder.addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(listType, "incoming")
                    .addParameter(listType, "outgoing")
                    .addParameter(String::class.java, "parentId")
                    .addParameter(String::class.java, "attachedToRef")
                    .addParameter(listType, "attachedElements")
                    .addStatement("this.incoming = incoming")
                    .addStatement("this.outgoing = outgoing")
                    .addStatement("this.parentId = parentId")
                    .addStatement("this.attachedToRef = attachedToRef")
                    .addStatement("this.attachedElements = attachedElements")
                    .build()
            )
            return builder.build()
        }
    }

    private inner class CallActivitiesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.CALL_ACTIVITIES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.callActivities.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val callActivitiesBuilder = TypeSpec.classBuilder("CallActivities").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.callActivities.forEach { callActivity -> callActivitiesBuilder.addField(createAttribute(callActivity)) }
            builder.addType(callActivitiesBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val messagesBuilder = TypeSpec.classBuilder("Messages").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.messages.forEach { message -> messagesBuilder.addField(createAttribute(message)) }
            builder.addType(messagesBuilder.build())
        }
    }

    private inner class ServiceTasksWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.serviceTasks.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val tasksBuilder = TypeSpec.classBuilder("TaskTypes").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.serviceTasks.forEach { task -> tasksBuilder.addField(createAttribute(task)) }
            builder.addType(tasksBuilder.build())
        }
    }

    private inner class SignalsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SIGNALS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.signals.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val signalsBuilder = TypeSpec.classBuilder("Signals").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.signals.forEach { signal -> signalsBuilder.addField(createAttribute(signal)) }
            builder.addType(signalsBuilder.build())
        }
    }

    private inner class VariablesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val variablesBuilder = TypeSpec.classBuilder("Variables").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.variables.forEach { variable -> variablesBuilder.addField(createAttribute(variable)) }
            modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val className = node.getRawName().toCamelCase()
                    val nodeVarsBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC, FINAL)
                    val sortedVariables = node.variables.sortedBy { it.getRawName() }
                    sortedVariables.forEach { nodeVarsBuilder.addField(createAttribute(it)) }
                    variablesBuilder.addType(nodeVarsBuilder.build())
                }
            builder.addType(variablesBuilder.build())
        }
    }

    private class ErrorsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.errors.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val errorsBuilder = TypeSpec.classBuilder("Errors").addModifiers(PUBLIC, STATIC, FINAL)
            errorsBuilder.addType(buildErrorClass())
            modelApi.model.errors.forEach {
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

    private class EscalationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ESCALATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.escalations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val escalationsBuilder = TypeSpec.classBuilder("Escalations").addModifiers(PUBLIC, STATIC, FINAL)
            escalationsBuilder.addType(buildEscalationClass())
            modelApi.model.escalations.forEach {
                val (escalationName, escalationCode) = it.getValue()
                val instanceBuilder = FieldSpec.builder(ClassName.get("", "BpmnEscalation"), it.getName())
                val variable = instanceBuilder.addModifiers(PUBLIC, STATIC, FINAL)
                escalationsBuilder.addField(variable.initializer("new BpmnEscalation(\$S, \$S)", escalationName, escalationCode).build())
            }
            builder.addType(escalationsBuilder.build())
        }

        private fun buildEscalationClass(): TypeSpec {
            val builder = TypeSpec.classBuilder("BpmnEscalation").addModifiers(PUBLIC, STATIC)
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
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val timersBuilder = TypeSpec.classBuilder("Timers").addModifiers(PUBLIC, STATIC, FINAL)
            timersBuilder.addType(buildTimerClass())
            modelApi.model.timers.forEach {
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