package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.palantir.javapoet.ClassName
import com.palantir.javapoet.CodeBlock
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
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
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.utils.StringUtils.toCamelCase
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/**
 * Generates the type-safe API contract for a single BPMN process as a Java class file.
 * References shared BPMN types (BpmnTimer, BpmnError, etc.) from the `bpmn-to-code-runtime` artifact.
 */
class JavaProcessApiBuilder : CodeGenerationAdapter.AbstractProcessApiBuilder<TypeSpec.Builder>() {

    companion object {
        private const val RUNTIME_PACKAGE = "io.github.emaarco.bpmn.runtime"
    }

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
            val processIdClass = ClassName.get(RUNTIME_PACKAGE, "ProcessId")
            val fieldBuilder = FieldSpec.builder(processIdClass, "PROCESS_ID").addModifiers(PUBLIC, FINAL, STATIC)
            builder.addField(fieldBuilder.initializer("new \$T(\$S)", processIdClass, modelApi.model.processId).build())
        }
    }

    private class ProcessEngineWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.PROCESS_ENGINE
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnEngineClass = ClassName.get(RUNTIME_PACKAGE, "BpmnEngine")
            val fieldBuilder = FieldSpec.builder(bpmnEngineClass, "PROCESS_ENGINE")
                .addModifiers(PUBLIC, FINAL, STATIC)
                .initializer("\$T.\$L", bpmnEngineClass, modelApi.engine.name)
            builder.addField(fieldBuilder.build())
        }
    }

    private inner class ElementsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ELEMENTS
        override fun shouldWrite(modelApi: BpmnModelApi) = true

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val elementIdClass = ClassName.get(RUNTIME_PACKAGE, "ElementId")
            val elementsBuilder = TypeSpec.classBuilder("Elements").addModifiers(PUBLIC, STATIC, FINAL)
                .addJavadoc(
                    "BPMN element ids as declared in the source model.\n" +
                        "Typically used in process-level tests or when searching for tasks.\n" +
                        "Worker runtime code rarely needs these.\n"
                )
            modelApi.model.flowNodes.forEach { flowNode ->
                elementsBuilder.addField(createTypedAttribute(flowNode, elementIdClass))
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
            val model = modelApi.model as? MergedBpmnModel ?: return
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
        val bpmnFlowClass = ClassName.get(RUNTIME_PACKAGE, "BpmnFlow")
        val flowsBuilder = TypeSpec.classBuilder("Flows").addModifiers(PUBLIC, STATIC, FINAL)
            .addJavadoc(
                "Sequence flows between BPMN elements.\n" +
                    "Mainly useful for process-model tooling, tests, and AI-agent consumers reasoning about the process shape.\n" +
                    "Worker code typically does not need these.\n"
            )
        sequenceFlows.forEach { flow ->
            val initCode = buildFlowInitializer(bpmnFlowClass, flow.id ?: "", flow.flowName, flow.sourceRef, flow.targetRef, flow.conditionExpression, flow.isDefault)
            val fieldBuilder = FieldSpec.builder(bpmnFlowClass, flow.getName()).addModifiers(PUBLIC, STATIC, FINAL)
            flowsBuilder.addField(fieldBuilder.initializer(initCode).build())
        }
        return flowsBuilder.build()
    }

    private fun buildFlowInitializer(bpmnFlowClass: ClassName, id: String, name: String?, sourceRef: String, targetRef: String, condition: String?, isDefault: Boolean): CodeBlock {
        val nameBlock = if (name != null) CodeBlock.of("\$S", name) else CodeBlock.of("null")
        val conditionBlock = if (condition != null) CodeBlock.of("\$S", condition) else CodeBlock.of("null")
        return CodeBlock.builder()
            .add("new \$T(\$S, ", bpmnFlowClass, id)
            .add(nameBlock)
            .add(", \$S, \$S, ", sourceRef, targetRef)
            .add(conditionBlock)
            .add(", \$L)", isDefault)
            .build()
    }

    private fun buildRelationsClass(packagePath: String, flowNodes: List<FlowNodeDefinition>): TypeSpec {
        val bpmnRelationsClass = ClassName.get(RUNTIME_PACKAGE, "BpmnRelations")
        val relationsBuilder = TypeSpec.classBuilder("Relations").addModifiers(PUBLIC, STATIC, FINAL)
            .addJavadoc(
                "Per-element graph metadata (previousElements / followingElements / parentId / boundary attachments).\n" +
                    "Intended for tooling and tests, not worker runtime code.\n"
            )
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
        val nameBlock = if (node.displayName != null) CodeBlock.of("\$S", node.displayName) else CodeBlock.of("null")
        val parentIdBlock = if (node.parentId != null) CodeBlock.of("\$S", node.parentId) else CodeBlock.of("null")
        val attachedToRefBlock = if (node.attachedToRef != null) CodeBlock.of("\$S", node.attachedToRef) else CodeBlock.of("null")
        return CodeBlock.builder()
            .add("new \$T(", bpmnRelationsClass)
            .add(nameBlock)
            .add(", ")
            .add(javaListLiteral(node.previousElements))
            .add(", ")
            .add(javaListLiteral(node.followingElements))
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
            val processIdClass = ClassName.get(RUNTIME_PACKAGE, "ProcessId")
            val callActivitiesBuilder = TypeSpec.classBuilder("CallActivities").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.callActivities.forEach { callActivity ->
                callActivitiesBuilder.addField(createTypedAttribute(callActivity, processIdClass))
            }
            builder.addType(callActivitiesBuilder.build())
        }
    }

    private inner class MessagesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.MESSAGES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.messages.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val messageNameClass = ClassName.get(RUNTIME_PACKAGE, "MessageName")
            val messagesBuilder = TypeSpec.classBuilder("Messages").addModifiers(PUBLIC, STATIC, FINAL)
                .addJavadoc("BPMN message names used to correlate messages to running process instances.\n")
            modelApi.model.messages.forEach { message ->
                messagesBuilder.addField(createTypedAttribute(message, messageNameClass))
            }
            builder.addType(messagesBuilder.build())
        }
    }

    private inner class ServiceTasksWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.SERVICE_TASKS
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.serviceTasks.any { it.getRawName().isNotEmpty() }

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val tasksBuilder = TypeSpec.classBuilder("ServiceTasks").addModifiers(PUBLIC, STATIC, FINAL)
                .addJavadoc(
                    "Job worker task types used in {@code @JobWorker(type = ServiceTasks.X)} annotations.\n" +
                        "Kept as {@code public static final String} because annotation arguments must be compile-time constants.\n"
                )
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
            val signalNameClass = ClassName.get(RUNTIME_PACKAGE, "SignalName")
            val signalsBuilder = TypeSpec.classBuilder("Signals").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.signals.forEach { signal ->
                signalsBuilder.addField(createTypedAttribute(signal, signalNameClass))
            }
            builder.addType(signalsBuilder.build())
        }
    }

    private inner class VariablesWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.VARIABLES
        override fun shouldWrite(modelApi: BpmnModelApi) = modelApi.model.variables.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val variableNameClass = ClassName.get(RUNTIME_PACKAGE, "VariableName")
            val variablesBuilder = TypeSpec.classBuilder("Variables").addModifiers(PUBLIC, STATIC, FINAL)
                .addJavadoc(
                    "Process variables grouped by the BPMN element that declares them.\n" +
                        "Direction is encoded in each variable's wrapper type: {@code VariableName.Input}, {@code VariableName.Output}, or {@code VariableName.InOut} when the variable is both read and written by the same element.\n" +
                        "Consumer APIs that take a specific subtype (for example, a method accepting {@code VariableName.Output}) get compile-time direction enforcement.\n"
                )
            val nodesWithVariables = modelApi.model.flowNodes
                .filter { it.variables.isNotEmpty() }
                .sortedBy { it.getRawName() }
            for (node in nodesWithVariables) {
                val className = node.getRawName().toCamelCase()
                val nodeVarsBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC, FINAL)
                val variablesByName = node.variables.groupBy { it.getRawName() }
                val sortedNames = variablesByName.keys.sorted()
                for (rawName in sortedNames) {
                    val group = variablesByName.getValue(rawName)
                    val directions = group.map { it.direction }.toSet()
                    val subtype = VariableNameSubtype.chooseFor(directions)
                    nodeVarsBuilder.addField(createDirectionalAttribute(group.first(), subtype, variableNameClass))
                }
                variablesBuilder.addType(nodeVarsBuilder.build())
            }
            builder.addType(variablesBuilder.build())
        }

        private fun createDirectionalAttribute(variable: VariableDefinition, subtype: VariableNameSubtype, wrapperClass: ClassName): FieldSpec {
            val subtypeClass = wrapperClass.nestedClass(subtype.simpleName)
            return FieldSpec.builder(subtypeClass, variable.getName())
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer("new \$T(\$S)", subtypeClass, variable.getValue())
                .build()
        }
    }

    private class ErrorsWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.ERRORS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.errors.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnErrorClass = ClassName.get(RUNTIME_PACKAGE, "BpmnError")
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
            val bpmnEscalationClass = ClassName.get(RUNTIME_PACKAGE, "BpmnEscalation")
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
            val elementIdClass = ClassName.get(RUNTIME_PACKAGE, "ElementId")
            val compensationsBuilder = TypeSpec.classBuilder("Compensations").addModifiers(PUBLIC, STATIC, FINAL)
            modelApi.model.compensations.forEach { compensation ->
                compensationsBuilder.addField(createTypedAttribute(compensation, elementIdClass))
            }
            builder.addType(compensationsBuilder.build())
        }
    }

    private class TimersWriter : ObjectWriter<TypeSpec.Builder> {

        override val objectType = ApiObjectType.TIMERS
        override fun shouldWrite(modelApi: BpmnModelApi): Boolean = modelApi.model.timers.isNotEmpty()

        override fun write(builder: TypeSpec.Builder, modelApi: BpmnModelApi) {
            val bpmnTimerClass = ClassName.get(RUNTIME_PACKAGE, "BpmnTimer")
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

    private fun createTypedAttribute(variable: VariableMapping<String>, wrapperClass: ClassName): FieldSpec {
        return FieldSpec.builder(wrapperClass, variable.getName())
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer("new \$T(\$S)", wrapperClass, variable.getValue())
            .build()
    }
}
