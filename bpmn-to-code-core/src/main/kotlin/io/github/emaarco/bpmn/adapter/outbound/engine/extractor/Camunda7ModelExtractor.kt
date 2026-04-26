package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.constants.CamundaModelConstants
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElements
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.DomElementUtils.withAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.DomElementUtils.withElementName
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.extractAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.filterByType
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.MessageUtils.findAllMessagesWithSource
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findCompensateEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findEscalationEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSequenceFlows
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.extractVariantName
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_AFTER_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_BEFORE_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.EXCLUSIVE_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDirection
import io.github.emaarco.bpmn.adapter.outbound.engine.SecureBpmnParser
import io.github.emaarco.bpmn.domain.utils.StringUtils.removeExpressionSyntax
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.DomElement
import org.camunda.bpm.model.xml.instance.ModelElementInstance
class Camunda7ModelExtractor : EngineSpecificExtractor {

    private val implKindKey = ServiceTaskDefinition.IMPL_KIND_KEY
    private val implValueKey = ServiceTaskDefinition.IMPL_VALUE_KEY

    override fun extract(bytes: ByteArray): BpmnModel {
        val modelInstance = SecureBpmnParser.readModelFromBytes(bytes)
        val processId = modelInstance.getProcessId()
        val variantName = modelInstance.extractVariantName()
        val allMessages = findMessages(modelInstance)
        val allFlowNodes = modelInstance.findFlowNodes()
        val allSequenceFlows = modelInstance.findSequenceFlows()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        val callActivities = findCallActivities(modelInstance)
        val messageSendEvents = findMessageSendEvents(modelInstance)
        val signals = modelInstance.findSignalEventDefinitions()
        val errors = modelInstance.findErrorEventDefinition()
        val escalations = modelInstance.findEscalationEventDefinitions()
        val compensations = modelInstance.findCompensateEventDefinitions()
        val timers = modelInstance.findTimerEventDefinition()
        val variablesPerNode = extractVariablesPerNode(modelInstance)

        val asyncPerNode = extractAsyncPerNode(modelInstance)
        val allServiceTasks = serviceTasks + messageSendEvents
        val enrichedFlowNodes = enrichFlowNodes(allFlowNodes, allServiceTasks, callActivities, timers, variablesPerNode, asyncPerNode)

        return BpmnModel(
            processId = processId,
            variantName = variantName,
            flowNodes = enrichedFlowNodes,
            sequenceFlows = allSequenceFlows,
            messages = allMessages,
            signals = signals,
            errors = errors,
            escalations = escalations,
            compensations = compensations,
        )
    }

    private fun enrichFlowNodes(
        flowNodes: List<FlowNodeDefinition>,
        serviceTasks: List<ServiceTaskDefinition>,
        callActivities: List<CallActivityDefinition>,
        timers: List<TimerDefinition>,
        variablesPerNode: Map<String?, List<VariableDefinition>>,
        asyncPerNode: Map<String?, Map<String, Any?>>,
    ): List<FlowNodeDefinition> {
        val serviceTaskById = serviceTasks.associateBy { it.id }
        val callActivityById = callActivities.associateBy { it.id }
        val timerById = timers.associateBy { it.id }
        val attachedElementsById = flowNodes
            .filter { it.attachedToRef != null }
            .groupBy { it.attachedToRef!! }
            .mapValues { (_, nodes) -> nodes.mapNotNull { it.id } }
        return flowNodes.map { node ->
            val properties = resolveProperties(node.id, serviceTaskById, callActivityById, timerById)
            val variables = variablesPerNode[node.id] ?: emptyList()
            val attachedElements = attachedElementsById[node.id] ?: emptyList()
            val engineSpecificProperties = asyncPerNode[node.id] ?: emptyMap()
            node.copy(properties = properties, variables = variables, attachedElements = attachedElements, engineSpecificProperties = engineSpecificProperties)
        }
    }

    private fun extractAsyncPerNode(modelInstance: ModelInstance): Map<String?, Map<String, Any?>> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        return flowNodes.associate { node ->
            val nodeId = node.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val asyncBefore = runCatching { node.isCamundaAsyncBefore }.getOrDefault(false)
            val asyncAfter = runCatching { node.isCamundaAsyncAfter }.getOrDefault(false)
            val exclusive = runCatching { node.isCamundaExclusive }.getOrDefault(true)
            val props = buildMap<String, Any?> {
                if (asyncBefore) put(ASYNC_BEFORE_KEY, true)
                if (asyncAfter) put(ASYNC_AFTER_KEY, true)
                if (!exclusive) put(EXCLUSIVE_KEY, false)
            }
            nodeId to props
        }
    }

    private fun resolveProperties(
        nodeId: String?,
        serviceTasks: Map<String?, ServiceTaskDefinition>,
        callActivities: Map<String?, CallActivityDefinition>,
        timers: Map<String?, TimerDefinition>,
    ): FlowNodeProperties {
        serviceTasks[nodeId]?.let { return FlowNodeProperties.ServiceTask(it) }
        callActivities[nodeId]?.let { return FlowNodeProperties.CallActivity(it) }
        timers[nodeId]?.let { return FlowNodeProperties.Timer(it) }
        return FlowNodeProperties.None
    }

    private fun findMessages(modelInstance: ModelInstance): List<MessageDefinition> {
        return modelInstance.findAllMessagesWithSource().map { (elementId, name, _) ->
            MessageDefinition(id = elementId, name = name)
        }
    }

    private fun findCallActivities(modelInstance: ModelInstance): List<CallActivityDefinition> {
        val callActivities = modelInstance.getModelElementsByType(CallActivity::class.java)
        return callActivities.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val calledElement = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_CALLED_ELEMENT)
            CallActivityDefinition(id, calledElement)
        }
    }

    private fun getServiceTaskTypes(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val serviceTasks = modelInstance.getModelElementsByType(ServiceTask::class.java)
        return serviceTasks.map { it.toServiceTask() }
    }

    private fun ServiceTask.toServiceTask(): ServiceTaskDefinition {
        val taskId = this.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
        val (kind, implValue) = this.detectImplementation()
        return ServiceTaskDefinition(
            id = taskId,
            engineSpecificProperties = buildMap {
                put(implValueKey, implValue)
                put(implKindKey, kind)
            }
        )
    }

    private fun ServiceTask.detectImplementation(): Pair<String?, String?> = when {
        this.camundaTopic != null -> Camunda7ImplementationKind.EXTERNAL_TASK.name to this.camundaTopic
        this.camundaDelegateExpression != null -> Camunda7ImplementationKind.DELEGATE_EXPRESSION.name to this.camundaDelegateExpression
        this.camundaClass != null -> Camunda7ImplementationKind.JAVA_DELEGATE.name to this.camundaClass
        this.camundaExpression != null -> Camunda7ImplementationKind.EXPRESSION.name to this.camundaExpression
        else -> null to null
    }

    private fun findMessageSendEvents(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val messageEvents = modelInstance.getModelElementsByType(MessageEventDefinition::class.java)
        return messageEvents.mapNotNull { event ->
            val (kind, implValue) = event.detectImplementation()
            if (implValue == null) return@mapNotNull null
            val taskId = event.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            ServiceTaskDefinition(
                id = taskId,
                engineSpecificProperties = buildMap {
                    put(implValueKey, implValue)
                    put(implKindKey, kind)
                }
            )
        }
    }

    private fun MessageEventDefinition.detectImplementation(): Pair<String?, String?> = when {
        this.camundaTopic != null -> Camunda7ImplementationKind.EXTERNAL_TASK.name to this.camundaTopic
        this.camundaDelegateExpression != null -> Camunda7ImplementationKind.DELEGATE_EXPRESSION.name to this.camundaDelegateExpression
        this.camundaClass != null -> Camunda7ImplementationKind.JAVA_DELEGATE.name to this.camundaClass
        this.camundaExpression != null -> Camunda7ImplementationKind.EXPRESSION.name to this.camundaExpression
        else -> null to null
    }

    private fun extractVariablesPerNode(modelInstance: ModelInstance): Map<String?, List<VariableDefinition>> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        return flowNodes.associate { node ->
            val nodeId = node.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val extensions = node.findExtensionElements()
            val ioExtensions = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT)
            val ioVars = extractInputAndOutputVariables(ioExtensions)
            val multiInstanceVars = extractMultiInstanceVariables(listOf(node))
            val callActivityMappingVars = extractCallActivityMappingVariables(extensions)
            val propertiesExtensions = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTIES)
            val additionalVars = extractAdditionalVariables(propertiesExtensions)
            val allVars = (ioVars + multiInstanceVars + callActivityMappingVars + additionalVars)
                .map { (name, direction) -> name.removeExpressionSyntax() to direction }
            val distinctVars = allVars.distinct().map { (name, direction) -> VariableDefinition(name, direction) }
            nodeId to distinctVars
        }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<Pair<String, VariableDirection>> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val inputs = allChildElements
            .withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_PARAMETER)
            .mapNotNull { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME) }
            .filter { it.isNotBlank() }
            .map { it to VariableDirection.INPUT }
        val outputs = allChildElements
            .withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER)
            .mapNotNull { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME) }
            .filter { it.isNotBlank() }
            .map { it to VariableDirection.OUTPUT }
        return inputs + outputs
    }

    private fun extractAdditionalVariables(
        extensions: List<ModelElementInstance>
    ): List<Pair<String, VariableDirection>> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val propertyElements = allChildElements.withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTY)
        val inputs = readAdditionalVariableValues(propertyElements, CamundaModelConstants.ADDITIONAL_INPUT_VARIABLES_PROPERTY_NAME)
            .map { it to VariableDirection.INPUT }
        val outputs = readAdditionalVariableValues(propertyElements, CamundaModelConstants.ADDITIONAL_OUTPUT_VARIABLES_PROPERTY_NAME)
            .map { it to VariableDirection.OUTPUT }
        return inputs + outputs
    }

    private fun readAdditionalVariableValues(
        propertyElements: List<DomElement>,
        propertyName: String,
    ): List<String> {
        val filter = BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME to propertyName
        val matchingProperties = propertyElements.withAttribute(filter)
        val rawValues = matchingProperties.map { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_VALUE) }
        return rawValues.flatMap { it?.split(",") ?: emptyList() }.map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun extractCallActivityMappingVariables(
        extensions: List<ModelElementInstance>
    ): List<Pair<String, VariableDirection>> {
        val inElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_IN)
        val outElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_OUT)
        val sourceVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE)
        val sourceExprVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)
        val targetVars = outElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TARGET)
        val inputs = (sourceVars + sourceExprVars).map { it to VariableDirection.INPUT }
        val outputs = targetVars.map { it to VariableDirection.OUTPUT }
        return inputs + outputs
    }

    private fun extractMultiInstanceVariables(
        nodes: Collection<FlowNode>
    ): List<Pair<String, VariableDirection>> {
        val loops = nodes.flatMap { it.getChildElementsByType(MultiInstanceLoopCharacteristics::class.java) }
        val collectionVariables = loops.mapNotNull { it.camundaCollection }.map { it to VariableDirection.INPUT }
        val elementVariables = loops.mapNotNull { it.camundaElementVariable }.map { it to VariableDirection.INPUT }
        return collectionVariables + elementVariables
    }

}
