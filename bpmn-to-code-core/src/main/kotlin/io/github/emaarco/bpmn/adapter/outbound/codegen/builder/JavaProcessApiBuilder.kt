package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.palantir.javapoet.ClassName
import com.palantir.javapoet.CodeBlock
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.ParameterizedTypeName
import com.palantir.javapoet.TypeSpec
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
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/**
 * Generates the type-safe API contract for a single BPMN process as a Java class file.
 * References shared BPMN types (BpmnTimer, BpmnError, etc.) from the sibling `types/` package via import.
 */
class JavaProcessApiBuilder : CodeGenerationAdapter.AbstractProcessApiBuilder<TypeSpec.Builder>() {

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
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean {
            return modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val flowsClass = buildFlowsClass(modelApi.packagePath, modelApi.model.sequenceFlows)
            builder.addType(flowsClass)
        }
    }

    private inner class RelationsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.RELATIONS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean {
            return modelApi.model is BpmnModel && modelApi.model.sequenceFlows.isNotEmpty()
        }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val relationsClass = buildRelationsClass(modelApi.packagePath, modelApi.model.flowNodes)
            builder.addType(relationsClass)
        }
    }

    private inner class VariantsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIANTS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model is MergedBpmnModel

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val model = modelApi.model as MergedBpmnModel
            val variantsBuilder = TypeSpec.classBuilder("Variants").addModifiers(PUBLIC, STATIC, FINAL)
            model.variants.forEach { variant ->
                val variantClass = buildVariantClass(modelApi.packagePath, variant)
                variantsBuilder.addType(variantClass)
            }
            builder.addType(variantsBuilder.build())
        }

        private fun buildVariantClass(packagePath: String, variant: VariantData): TypeSpec {
            val variantName = variant.variantName.toCamelCase()
            val variantBuilder = TypeSpec.classBuilder(variantName).addModifiers(PUBLIC, STATIC, FINAL)
            if (variant.sequenceFlows.isNotEmpty()) {
                variantBuilder.addType(buildFlowsClass(packagePath, variant.sequenceFlows))
                variantBuilder.addType(buildRelationsClass(packagePath, variant.flowNodes))
            }
            return variantBuilder.build()
        }
    }

    private fun buildFlowsClass(packagePath: String, sequenceFlows: List<SequenceFlowDefinition>): TypeSpec {
        val bpmnFlowClass = ClassName.get("${packagePath}.types", "BpmnFlow")
        val flowsBuilder = TypeSpec.classBuilder("Flows").addModifiers(PUBLIC, STATIC, FINAL)
        sequenceFlows.forEach { flow ->
            val initCode = buildFlowInitializer(bpmnFlowClass, flow.id ?: "", flow.sourceRef, flow.targetRef, flow.conditionExpression, flow.isDefault)
            val fieldBuilder = FieldSpec.builder(bpmnFlowClass, flow.getName()).addModifiers(PUBLIC, STATIC, FINAL)
            flowsBuilder.addField(fieldBuilder.initializer(initCode).build())
        }
        return flowsBuilder.build()
    }

    private fun buildFlowInitializer(bpmnFlowClass: ClassName, id: String, sourceRef: String, targetRef: String, condition: String?, isDefault: Boolean): CodeBlock {
        val conditionBlock = if (condition != null) CodeBlock.of("\$S", condition) else CodeBlock.of("null")
        return CodeBlock.builder()
            .add("new \$T(\$S, \$S, \$S, ", bpmnFlowClass, id, sourceRef, targetRef)
            .add(conditionBlock)
            .add(", \$L)", isDefault)
            .build()
    }

    private fun buildRelationsClass(packagePath: String, flowNodes: List<FlowNodeDefinition>): TypeSpec {
        val bpmnRelationsClass = ClassName.get("${packagePath}.types", "BpmnRelations")
        val relationsBuilder = TypeSpec.classBuilder("Relations").addModifiers(PUBLIC, STATIC, FINAL)
        flowNodes
            .filter { it.id != null }
            .sortedBy { it.getRawName() }
            .forEach { node ->
                val initCode = buildRelationsInitializer(bpmnRelationsClass, node)
                val fieldBuilder = FieldSpec.builder(bpmnRelationsClass, node.getName()).addModifiers(PUBLIC, STATIC, FINAL)
                relationsBuilder.addField(fieldBuilder.initializer(initCode).build())
            }
        return relationsBuilder.build()
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

    private val listClass = ClassName.get("java.util", "List")

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
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.serviceTasks.any { it.getRawName().isNotEmpty() }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val tasksBuilder = TypeSpec.classBuilder("TaskTypes").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.serviceTasks
                .filter { it.getRawName().isNotEmpty() }
                .forEach { task -> tasksBuilder.addField(createAttribute(task)) }
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
