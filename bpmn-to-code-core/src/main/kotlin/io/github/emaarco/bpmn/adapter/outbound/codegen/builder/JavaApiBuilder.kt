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
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
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
        val typeSpec = TypeSpec.classBuilder("BpmnTimer").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "type").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "timerValue").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "type")
                    .addParameter(String::class.java, "timerValue")
                    .addStatement("this.type = type")
                    .addStatement("this.timerValue = timerValue")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnTimer", typeSpec, language)
    }

    private fun buildBpmnErrorFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnError").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "code").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "name")
                    .addParameter(String::class.java, "code")
                    .addStatement("this.name = name")
                    .addStatement("this.code = code")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnError", typeSpec, language)
    }

    private fun buildBpmnEscalationFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnEscalation").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "code").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "name")
                    .addParameter(String::class.java, "code")
                    .addStatement("this.name = name")
                    .addStatement("this.code = code")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnEscalation", typeSpec, language)
    }

    private fun buildBpmnFlowFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnFlow").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "id").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "sourceRef").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "targetRef").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "condition").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(Boolean::class.javaPrimitiveType!!, "isDefault").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
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
            .build()
        return buildTypeFile(typesPackage, "BpmnFlow", typeSpec, language)
    }

    private fun buildBpmnRelationsFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val listClass = ClassName.get("java.util", "List")
        val listType = ParameterizedTypeName.get(listClass, ClassName.get("java.lang", "String"))
        val typeSpec = TypeSpec.classBuilder("BpmnRelations").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(listType, "incoming").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(listType, "outgoing").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "parentId").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "attachedToRef").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(listType, "attachedElements").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
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
            .build()
        return buildTypeFile(typesPackage, "BpmnRelations", typeSpec, language)
    }

    private fun buildTypeFile(typesPackage: String, className: String, typeSpec: TypeSpec, language: OutputLanguage): GeneratedApiFile {
        val javaFile = JavaFile.builder(typesPackage, typeSpec)
            .addFileComment(autoGenComment)
            .build()
        val content = buildString { javaFile.writeTo(this) }
        return GeneratedApiFile(
            fileName = "$className.java",
            packagePath = typesPackage,
            content = content,
            language = language,
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
            val bpmnFlowClass = ClassName.get("${modelApi.packagePath}.types", "BpmnFlow")
            val flowsBuilder = TypeSpec.classBuilder("Flows").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.sequenceFlows.forEach { flow ->
                val initCode = buildFlowInitializer(bpmnFlowClass, flow.id ?: "", flow.sourceRef, flow.targetRef, flow.conditionExpression, flow.isDefault)
                val fieldBuilder = FieldSpec.builder(bpmnFlowClass, flow.getName()).addModifiers(PUBLIC, STATIC, FINAL)
                flowsBuilder.addField(fieldBuilder.initializer(initCode).build())
            }
            builder.addType(flowsBuilder.build())
        }

        private fun buildFlowInitializer(bpmnFlowClass: ClassName, id: String, sourceRef: String, targetRef: String, condition: String?, isDefault: Boolean): CodeBlock {
            val conditionBlock = if (condition != null) CodeBlock.of("\$S", condition) else CodeBlock.of("null")
            return CodeBlock.builder()
                .add("new \$T(\$S, \$S, \$S, ", bpmnFlowClass, id, sourceRef, targetRef)
                .add(conditionBlock)
                .add(", \$L)", isDefault)
                .build()
        }
    }

    private inner class RelationsWriter : ObjectWriter<TypeSpec.Builder> {

        private val listClass = ClassName.get("java.util", "List")
        private val listType = ParameterizedTypeName.get(listClass, ClassName.get("java.lang", "String"))

        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.sequenceFlows.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnRelationsClass = ClassName.get("${modelApi.packagePath}.types", "BpmnRelations")
            val relationsBuilder = TypeSpec.classBuilder("Relations").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.flowNodes
                .filter { it.id != null }
                .sortedBy { it.getRawName() }
                .forEach { node ->
                    val initCode = buildRelationsInitializer(bpmnRelationsClass, node)
                    val fieldBuilder = FieldSpec.builder(bpmnRelationsClass, node.getName()).addModifiers(PUBLIC, STATIC, FINAL)
                    relationsBuilder.addField(fieldBuilder.initializer(initCode).build())
                }
            builder.addType(relationsBuilder.build())
        }

        private fun buildRelationsInitializer(bpmnRelationsClass: ClassName, node: FlowNodeDefinition): CodeBlock {
            val parentIdBlock = if (node.parentId != null) CodeBlock.of("\$S", node.parentId) else CodeBlock.of("null")
            val attachedToRefBlock = if (node.attachedToRef != null) CodeBlock.of("\$S", node.attachedToRef) else CodeBlock.of("null")
            return CodeBlock.builder()
                .add("new \$T(", bpmnRelationsClass)
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
            val bpmnErrorClass = ClassName.get("${modelApi.packagePath}.types", "BpmnError")
            val errorsBuilder = TypeSpec.classBuilder("Errors").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.errors.forEach {
                val (errorName, errorCode) = it.getValue()
                val instanceBuilder = FieldSpec.builder(bpmnErrorClass, it.getName())
                val variable = instanceBuilder.addModifiers(PUBLIC, STATIC, FINAL)
                errorsBuilder.addField(variable.initializer("new \$T(\$S, \$S)", bpmnErrorClass, errorName, errorCode).build())
            }
            builder.addType(errorsBuilder.build())
        }
    }

    private class EscalationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ESCALATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.escalations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnEscalationClass = ClassName.get("${modelApi.packagePath}.types", "BpmnEscalation")
            val escalationsBuilder = TypeSpec.classBuilder("Escalations").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.escalations.forEach {
                val (escalationName, escalationCode) = it.getValue()
                val instanceBuilder = FieldSpec.builder(bpmnEscalationClass, it.getName())
                val variable = instanceBuilder.addModifiers(PUBLIC, STATIC, FINAL)
                escalationsBuilder.addField(variable.initializer("new \$T(\$S, \$S)", bpmnEscalationClass, escalationName, escalationCode).build())
            }
            builder.addType(escalationsBuilder.build())
        }
    }

    private inner class CompensationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.COMPENSATIONS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.compensations.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val compensationsBuilder = TypeSpec.classBuilder("Compensations").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.compensations.forEach { compensation ->
                compensationsBuilder.addField(createAttribute(compensation))
            }
            builder.addType(compensationsBuilder.build())
        }
    }

    private class TimersWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnTimerClass = ClassName.get("${modelApi.packagePath}.types", "BpmnTimer")
            val timersBuilder = TypeSpec.classBuilder("Timers").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.timers.forEach {
                val (timerType, timerValue) = it.getValue()
                val instanceBuilder = FieldSpec.builder(bpmnTimerClass, it.getName())
                val variable = instanceBuilder.addModifiers(PUBLIC, STATIC, FINAL)
                timersBuilder.addField(variable.initializer("new \$T(\$S, \$S)", bpmnTimerClass, timerType, timerValue).build())
            }
            builder.addType(timersBuilder.build())
        }
    }

    private fun createAttribute(variable: VariableMapping<*>): FieldSpec {
        return FieldSpec.builder(String::class.java, variable.getName())
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer("\$S", variable.getValue())
            .build()
    }
}
