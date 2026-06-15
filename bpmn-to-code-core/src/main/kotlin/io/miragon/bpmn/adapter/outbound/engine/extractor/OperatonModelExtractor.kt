package io.miragon.bpmn.adapter.outbound.engine.extractor

import io.miragon.bpmn.adapter.outbound.engine.constants.CamundaModelConstants
import io.miragon.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElements
import io.miragon.bpmn.adapter.outbound.engine.utils.DomElementUtils.withAttribute
import io.miragon.bpmn.adapter.outbound.engine.utils.DomElementUtils.withElementName
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.extractAttribute
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.filterByType
import io.miragon.bpmn.adapter.outbound.engine.utils.MessageUtils.findAllMessagesWithSource
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findCompensateEventDefinitions
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findEscalationEventDefinitions
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSequenceFlows
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.extractVariantName
import io.miragon.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.miragon.bpmn.domain.BpmnModel
import io.miragon.bpmn.domain.shared.CallActivityDefinition
import io.miragon.bpmn.domain.shared.CallActivityMapping
import io.miragon.bpmn.domain.shared.FlowNodeDefinition
import io.miragon.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_AFTER_KEY
import io.miragon.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_BEFORE_KEY
import io.miragon.bpmn.domain.shared.FlowNodeDefinition.Companion.EXCLUSIVE_KEY
import io.miragon.bpmn.domain.shared.FlowNodeProperties
import io.miragon.bpmn.domain.shared.MessageDefinition
import io.miragon.bpmn.domain.shared.ServiceTaskDefinition
import io.miragon.bpmn.domain.shared.TimerDefinition
import io.miragon.bpmn.domain.shared.VariableDefinition
import io.miragon.bpmn.domain.shared.VariableDirection
import io.miragon.bpmn.adapter.outbound.engine.EngineDetector
import io.miragon.bpmn.adapter.outbound.engine.SecureBpmnParser
import io.miragon.bpmn.domain.utils.StringUtils.removeExpressionSyntax
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.DomElement
import org.camunda.bpm.model.xml.instance.ModelElementInstance
@Suppress("TooManyFunctions")
class OperatonModelExtractor : EngineSpecificExtractor {

    private val implKindKey = ServiceTaskDefinition.IMPL_KIND_KEY
    private val implValueKey = ServiceTaskDefinition.IMPL_VALUE_KEY

    companion object {
        private const val NAMESPACE = "http://operaton.org/schema/1.0/bpmn"
    }

    override fun extract(bytes: ByteArray): BpmnModel {
        val modelInstance = SecureBpmnParser.readModelFromBytes(bytes)
        val processId = modelInstance.getProcessId()
        val variantName = modelInstance.extractVariantName()
        val messages = findMessages(modelInstance)
        val flowNodes = modelInstance.findFlowNodes()
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
        val enrichedFlowNodes = enrichFlowNodes(flowNodes, allServiceTasks, callActivities, timers, variablesPerNode, asyncPerNode)

        return BpmnModel(
            processId = processId,
            variantName = variantName,
            flowNodes = enrichedFlowNodes,
            sequenceFlows = allSequenceFlows,
            messages = messages,
            signals = signals,
            errors = errors,
            escalations = escalations,
            compensations = compensations,
            detectedEngine = EngineDetector.detect(bytes.decodeToString()),
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
            val asyncBefore = node.getAttributeValueNs(NAMESPACE, "asyncBefore")?.toBoolean() ?: false
            val asyncAfter = node.getAttributeValueNs(NAMESPACE, "asyncAfter")?.toBoolean() ?: false
            val exclusive = node.getAttributeValueNs(NAMESPACE, "exclusive")?.toBoolean()
            val props = buildMap<String, Any?> {
                if (asyncBefore) put(ASYNC_BEFORE_KEY, true)
                if (asyncAfter) put(ASYNC_AFTER_KEY, true)
                if (exclusive == false) put(EXCLUSIVE_KEY, false)
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
        return callActivities.map { activity ->
            val id = activity.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val calledElement = activity.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_CALLED_ELEMENT)
            val extensions = activity.findExtensionElements()
            val propagateAllInput = extractPropagateAll(extensions, BpmnModelConstants.CAMUNDA_ELEMENT_IN)
            val propagateAllOutput = extractPropagateAll(extensions, BpmnModelConstants.CAMUNDA_ELEMENT_OUT)
            CallActivityDefinition(
                id = id,
                calledElement = calledElement,
                mappings = extractCallActivityMappings(extensions),
                engineSpecificProperties = buildMap {
                    propagateAllInput?.let { put(CallActivityDefinition.PROPAGATE_ALL_INPUT_KEY, it) }
                    propagateAllOutput?.let { put(CallActivityDefinition.PROPAGATE_ALL_OUTPUT_KEY, it) }
                },
            )
        }
    }

    private fun extractCallActivityMappings(
        extensions: List<ModelElementInstance>
    ): List<CallActivityMapping> {
        val rawInputs = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_IN)
        val inputs = rawInputs.mapNotNull { it.domElement.toCallActivityMapping(VariableDirection.INPUT) }
        val rawOutputs = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_OUT)
        val outputs = rawOutputs.mapNotNull { it.domElement.toCallActivityMapping(VariableDirection.OUTPUT) }
        return inputs + outputs
    }

    private fun DomElement.toCallActivityMapping(direction: VariableDirection): CallActivityMapping? {
        val source = getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE)?.takeIf { it.isNotBlank() }
        val sourceExpression = getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)?.takeIf { it.isNotBlank() }
        val target = getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TARGET)?.takeIf { it.isNotBlank() }
        if (source == null && sourceExpression == null && target == null) return null
        return CallActivityMapping(direction, source, sourceExpression, target)
    }

    private fun extractPropagateAll(
        extensions: List<ModelElementInstance>,
        elementType: String,
    ): Boolean? {
        val hasAll = extensions.filterByType(elementType).any {
            it.domElement.getAttribute(CamundaModelConstants.VARIABLES_ATTRIBUTE) == CamundaModelConstants.VARIABLES_ALL_VALUE
        }
        return if (hasAll) true else null
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

    private fun ServiceTask.detectImplementation(): Pair<String?, String?> {
        val extractor = { attrName: String -> this.getAttributeValueNs(NAMESPACE, attrName) }
        val delegateExpression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
        val javaClass = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS)
        val topic = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TOPIC)
        val expression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_EXPRESSION)
        return when {
            delegateExpression != null -> OperatonImplementationKind.DELEGATE_EXPRESSION.name to delegateExpression
            javaClass != null -> OperatonImplementationKind.JAVA_DELEGATE.name to javaClass
            topic != null -> OperatonImplementationKind.EXTERNAL_TASK.name to topic
            expression != null -> OperatonImplementationKind.EXPRESSION.name to expression
            else -> null to null
        }
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

    private fun MessageEventDefinition.detectImplementation(): Pair<String?, String?> {
        val extractor = { attrName: String -> this.getAttributeValueNs(NAMESPACE, attrName) }
        val topic = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TOPIC)
        val delegateExpression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
        val javaClass = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS)
        val expression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_EXPRESSION)
        return when {
            topic != null -> OperatonImplementationKind.EXTERNAL_TASK.name to topic
            delegateExpression != null -> OperatonImplementationKind.DELEGATE_EXPRESSION.name to delegateExpression
            javaClass != null -> OperatonImplementationKind.JAVA_DELEGATE.name to javaClass
            expression != null -> OperatonImplementationKind.EXPRESSION.name to expression
            else -> null to null
        }
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
                .map { (name, direction, expression) -> Triple(name.removeExpressionSyntax(), direction, expression) }
            val distinctVars = allVars.distinct().map { (name, direction, expression) -> VariableDefinition(name, direction, expression) }
            nodeId to distinctVars
        }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<Triple<String, VariableDirection, String?>> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val inputs = allChildElements
            .withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_PARAMETER)
            .mapNotNull { it.toVariableMapping(VariableDirection.INPUT) }
        val outputs = allChildElements
            .withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER)
            .mapNotNull { it.toVariableMapping(VariableDirection.OUTPUT) }
        return inputs + outputs
    }

    /**
     * Maps an operaton:inputParameter / operaton:outputParameter element to a variable mapping.
     * The variable name comes from the 'name' attribute, while the value expression (right-hand side)
     * is the element's raw text content, e.g. `${execution.getVariable('x')}`.
     */
    private fun DomElement.toVariableMapping(
        direction: VariableDirection
    ): Triple<String, VariableDirection, String?>? {
        val name = this.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME)?.takeIf { it.isNotBlank() }
            ?: return null
        val expression = this.textContent?.trim()?.takeIf { it.isNotBlank() }
        return Triple(name, direction, expression)
    }

    private fun extractAdditionalVariables(
        extensions: List<ModelElementInstance>
    ): List<Triple<String, VariableDirection, String?>> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val propertyElements = allChildElements.withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTY)
        val inputs = readAdditionalVariableValues(propertyElements, CamundaModelConstants.ADDITIONAL_INPUT_VARIABLES_PROPERTY_NAME)
            .map { Triple(it, VariableDirection.INPUT, null) }
        val outputs = readAdditionalVariableValues(propertyElements, CamundaModelConstants.ADDITIONAL_OUTPUT_VARIABLES_PROPERTY_NAME)
            .map { Triple(it, VariableDirection.OUTPUT, null) }
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
    ): List<Triple<String, VariableDirection, String?>> {
        val inElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_IN)
        val outElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_OUT)
        val sourceVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE)
        val sourceExprVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)
        val targetVars = outElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TARGET)
        val inputs = (sourceVars + sourceExprVars).map { Triple(it, VariableDirection.INPUT, it) }
        val outputs = targetVars.map { Triple(it, VariableDirection.OUTPUT, it) }
        return inputs + outputs
    }

    private fun extractMultiInstanceVariables(
        nodes: Collection<FlowNode>
    ): List<Triple<String, VariableDirection, String?>> {
        val loops = nodes.flatMap { it.getChildElementsByType(MultiInstanceLoopCharacteristics::class.java) }
        val collectionVariables = loops
            .extractVariablesFromLoops(BpmnModelConstants.CAMUNDA_ATTRIBUTE_COLLECTION)
            .map { Triple(it, VariableDirection.INPUT, it) }
        val elementVariables = loops
            .extractVariablesFromLoops(BpmnModelConstants.CAMUNDA_ATTRIBUTE_ELEMENT_VARIABLE)
            .map { Triple(it, VariableDirection.INPUT, it) }
        return collectionVariables + elementVariables
    }

    private fun List<MultiInstanceLoopCharacteristics>.extractVariablesFromLoops(
        variableType: String
    ): List<String> {
        return this.mapNotNull { it.getAttributeValueNs(NAMESPACE, variableType) }
    }

}
