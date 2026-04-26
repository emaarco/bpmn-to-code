package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.constants.ZeebeModelConstants
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElement
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElements
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElementsWithType
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.extractAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.filterByType
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.findFirstByType
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
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.SecureBpmnParser
import io.github.emaarco.bpmn.domain.shared.VariableDirection
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.Message
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.io.InputStream

class ZeebeModelExtractor : EngineSpecificExtractor {

    private val implKindKey = ServiceTaskDefinition.IMPL_KIND_KEY
    private val implValueKey = ServiceTaskDefinition.IMPL_VALUE_KEY

    override fun extract(inputStream: InputStream): BpmnModel {
        val modelInstance = SecureBpmnParser.readModelFromStream(inputStream)
        val processId = modelInstance.getProcessId()
        val variantName = modelInstance.extractVariantName()
        val allFlowNodes = modelInstance.findFlowNodes()
        val allSequenceFlows = modelInstance.findSequenceFlows()
        val allMessages = extractZeebeMessages(modelInstance)
        val allErrorEvents = modelInstance.findErrorEventDefinition()
        val allEscalationEvents = modelInstance.findEscalationEventDefinitions()
        val allCompensationEvents = modelInstance.findCompensateEventDefinitions()
        val allTimerEvents = modelInstance.findTimerEventDefinition()
        val allSignalEvents = modelInstance.findSignalEventDefinitions()
        val allServiceTasks = findServiceTasks(modelInstance)
        val allCallActivities = findCallActivities(modelInstance)
        val variablesPerNode = extractVariablesPerNode(modelInstance)

        val enrichedFlowNodes = enrichFlowNodes(allFlowNodes, allServiceTasks, allCallActivities, allTimerEvents, variablesPerNode)

        return BpmnModel(
            processId = processId,
            variantName = variantName,
            flowNodes = enrichedFlowNodes,
            sequenceFlows = allSequenceFlows,
            messages = allMessages,
            signals = allSignalEvents,
            errors = allErrorEvents,
            escalations = allEscalationEvents,
            compensations = allCompensationEvents,
        )
    }

    private fun enrichFlowNodes(
        flowNodes: List<FlowNodeDefinition>,
        serviceTasks: List<ServiceTaskDefinition>,
        callActivities: List<CallActivityDefinition>,
        timers: List<io.github.emaarco.bpmn.domain.shared.TimerDefinition>,
        variablesPerNode: Map<String?, List<VariableDefinition>>,
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
            node.copy(properties = properties, variables = variables, attachedElements = attachedElements)
        }
    }

    private fun resolveProperties(
        nodeId: String?,
        serviceTasks: Map<String?, ServiceTaskDefinition>,
        callActivities: Map<String?, CallActivityDefinition>,
        timers: Map<String?, io.github.emaarco.bpmn.domain.shared.TimerDefinition>,
    ): FlowNodeProperties {
        serviceTasks[nodeId]?.let { return FlowNodeProperties.ServiceTask(it) }
        callActivities[nodeId]?.let { return FlowNodeProperties.CallActivity(it) }
        timers[nodeId]?.let { return FlowNodeProperties.Timer(it) }
        return FlowNodeProperties.None
    }

    private fun findCallActivities(modelInstance: ModelInstance): List<CallActivityDefinition> {
        val callActivities = modelInstance.getModelElementsByType(CallActivity::class.java)
        return callActivities.map { activity ->
            val elementId = activity.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val extension = activity.findExtensionElement(BpmnModelConstants.BPMN_ATTRIBUTE_CALLED_ELEMENT)
            val processId = extension.getAttributeValue(ZeebeModelConstants.ATTRIBUTE_PROCESS_ID)
            CallActivityDefinition(elementId, processId)
        }
    }

    private fun findServiceTasks(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        return flowNodes.mapNotNull { node ->
            val extensionElements = node.findExtensionElements()
            val taskDefinition = extensionElements.findFirstByType(ZeebeModelConstants.ELEMENT_TASK_DEFINITION)
                ?: return@mapNotNull null
            val id = node.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val type = taskDefinition.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_TYPE)
                ?.takeIf { it.isNotBlank() }
            ServiceTaskDefinition(
                id = id,
                engineSpecificProperties = buildMap {
                    put(implValueKey, type)
                    put(implKindKey, ZeebeImplementationKind.JOB_WORKER.name)
                }
            )
        }
    }

    private fun extractZeebeMessages(modelInstance: ModelInstance): List<MessageDefinition> {
        return modelInstance.findAllMessagesWithSource().map { (elementId, name, message) ->
            val engineSpecificProperties = message?.zeebeSubscriptionProperties() ?: emptyMap()
            MessageDefinition(id = elementId, name = name, engineSpecificProperties = engineSpecificProperties)
        }
    }

    private fun Message.zeebeSubscriptionProperties(): Map<String, Any?> {
        val subscription = this.findExtensionElementsWithType(ZeebeModelConstants.ELEMENT_SUBSCRIPTION).firstOrNull()
            ?: return emptyMap()
        val correlationKey = subscription.getAttributeValue(ZeebeModelConstants.ATTRIBUTE_CORRELATION_KEY)
            ?: return emptyMap()
        return mapOf(ZeebeModelConstants.ATTRIBUTE_CORRELATION_KEY to correlationKey)
    }

    private fun extractVariablesPerNode(modelInstance: ModelInstance): Map<String?, List<VariableDefinition>> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        return flowNodes.associate { node ->
            val nodeId = node.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val ioMappings = node.findExtensionElementsWithType(ZeebeModelConstants.ELEMENT_IO_MAPPING)
            val inputs = extractIoVariables(ioMappings, ZeebeModelConstants.ELEMENT_INPUT, VariableDirection.INPUT)
            val outputs = extractIoVariables(ioMappings, ZeebeModelConstants.ELEMENT_OUTPUT, VariableDirection.OUTPUT)
            val multiInstanceVars = extractMultiInstanceVariables(listOf(node))
            val allVars = inputs + outputs + multiInstanceVars
            val distinctVars = allVars.distinct().map { (name, direction) -> VariableDefinition(name, direction) }
            nodeId to distinctVars
        }
    }

    private fun extractIoVariables(
        extensions: List<ModelElementInstance>,
        elementName: String,
        direction: VariableDirection,
    ): List<Pair<String, VariableDirection>> {
        val allElementsInContainer = extensions.flatMap { it.domElement.childElements }
        val matching = allElementsInContainer.filter { it.localName == elementName }
        return matching
            .mapNotNull { it.getAttribute(ZeebeModelConstants.ATTRIBUTE_TARGET) }
            .filter { it.isNotBlank() }
            .map { it to direction }
    }

    private fun extractMultiInstanceVariables(
        nodes: Collection<FlowNode>
    ): List<Pair<String, VariableDirection>> {
        val loops = nodes.flatMap { it.getChildElementsByType(MultiInstanceLoopCharacteristics::class.java) }
        val allExtensions = loops.flatMap { it.findExtensionElements() }
        val loopCharacteristics = allExtensions.filterByType(ZeebeModelConstants.ELEMENT_LOOP_CHARACTERISTICS)
        val inputElements = loopCharacteristics.extractAttribute(ZeebeModelConstants.ATTRIBUTE_INPUT_ELEMENT)
        val inputCollections = loopCharacteristics.extractAttribute(ZeebeModelConstants.ATTRIBUTE_INPUT_COLLECTION)
        val outputElements = loopCharacteristics.extractAttribute(ZeebeModelConstants.ATTRIBUTE_OUTPUT_ELEMENT)
        val outputCollections = loopCharacteristics.extractAttribute(ZeebeModelConstants.ATTRIBUTE_OUTPUT_COLLECTION)
        val inputs = (inputElements + inputCollections).map { it.removePrefix("=") to VariableDirection.INPUT }
        val outputs = (outputElements + outputCollections).map { it.removePrefix("=") to VariableDirection.OUTPUT }
        return inputs + outputs
    }

}
