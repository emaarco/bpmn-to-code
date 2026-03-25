package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.constants.CamundaModelConstants
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElements
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.extractAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.filterByType
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findMessages
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.utils.StringUtils.removeExpressionSyntax
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.io.InputStream

/**
 * Model extractor for Operaton BPMN engine
 * If you are using operaton, but your models are still camunda-7 based, you cannot use this extractor.
 * Instead, you must use the [Camunda7ModelExtractor].
 */
class OperatonModelExtractor : EngineSpecificExtractor {

    companion object {
        private const val NAMESPACE = "http://operaton.org/schema/1.0/bpmn"
    }

    override fun extract(inputStream: InputStream): BpmnModel {
        val modelInstance = Bpmn.readModelFromStream(inputStream)
        val processId = modelInstance.getProcessId()
        val messages = modelInstance.findMessages()
        val flowNodes = modelInstance.findFlowNodes()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        val callActivities = findCallActivities(modelInstance)
        val messageSendEvents = findMessageSendEvents(modelInstance)
        val signals = modelInstance.findSignalEventDefinitions()
        val errors = modelInstance.findErrorEventDefinition()
        val timers = modelInstance.findTimerEventDefinition()
        val variables = extractVariables(modelInstance)
        return BpmnModel(
            processId = processId,
            flowNodes = flowNodes,
            callActivities = callActivities,
            serviceTasks = serviceTasks + messageSendEvents,
            messages = messages,
            signals = signals,
            errors = errors,
            timers = timers,
            variables = variables
        )
    }

    private fun findCallActivities(modelInstance: ModelInstance): List<CallActivityDefinition> {
        val callActivities = modelInstance.getModelElementsByType(CallActivity::class.java)
        return callActivities.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val calledElement = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_CALLED_ELEMENT)
            requireNotNull(id) { "CallActivity is missing an 'id' attribute" }
            requireNotNull(calledElement) { "CallActivity '$id' is missing a 'calledElement' attribute" }
            CallActivityDefinition(id, calledElement)
        }
    }

    private fun getServiceTaskTypes(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val serviceTasks = modelInstance.getModelElementsByType(ServiceTask::class.java)
        return serviceTasks.map { it.toServiceTask() }
    }

    private fun ServiceTask.toServiceTask(): ServiceTaskDefinition {
        val taskId = this.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
        val workerType = this.detectWorkerType()
        return ServiceTaskDefinition(id = taskId, type = workerType)
    }

    private fun ServiceTask.detectWorkerType(): String {
        val taskExtractor = { attrName: String -> this.getAttributeValueNs(NAMESPACE, attrName) }
        val delegateBasedTask = taskExtractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
        val classBasedTask = taskExtractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS)
        val topicBasedTask = taskExtractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TOPIC)
        return when {
            delegateBasedTask != null -> delegateBasedTask
            classBasedTask != null -> classBasedTask
            topicBasedTask != null -> topicBasedTask
            else -> error("Service task '${this.id}' has no valid worker type")
        }
    }

    private fun findMessageSendEvents(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val messageEvents = modelInstance.getModelElementsByType(MessageEventDefinition::class.java)
        val sendEvents = messageEvents.mapNotNull { it.detectSendEvents() }
        return sendEvents.map { (type, event) ->
            val taskId = event.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            requireNotNull(taskId) { "The element the MessageEventDefinition belongs to has no 'id' defined" }
            ServiceTaskDefinition(id = taskId, type = type)
        }
    }

    private fun MessageEventDefinition.detectSendEvents(): Pair<String, MessageEventDefinition>? {
        val eventExtractor = { attrName: String -> this.getAttributeValueNs(NAMESPACE, attrName) }
        val topicBasedEvent = eventExtractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TOPIC)
        val delegateBasedEvent = eventExtractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
        val classBasedEvent = eventExtractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS)
        return when {
            topicBasedEvent != null -> topicBasedEvent to this
            delegateBasedEvent != null -> delegateBasedEvent to this
            classBasedEvent != null -> classBasedEvent to this
            else -> null
        }
    }

    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val allExtensions = flowNodes.flatMap { it.findExtensionElements() }
        val matchingExtensions = allExtensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT)
        val ioVariableNames = extractInputAndOutputVariables(matchingExtensions)
        val multiInstanceVariableNames = extractMultiInstanceVariables(flowNodes)
        val callActivityMappingVars = extractCallActivityMappingVariables(allExtensions)
        val allVariableNames = ioVariableNames + multiInstanceVariableNames + callActivityMappingVars
        return allVariableNames.map { it.removeExpressionSyntax() }.distinct().map { VariableDefinition(it) }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allowedDefinitions = CamundaModelConstants.inputOutputParameters
        val allElementsInContainer = extensions.flatMap { it.domElement.childElements }
        val eitherInputOrOutput = allElementsInContainer.filter { allowedDefinitions.contains(it.localName) }
        val variableNames = eitherInputOrOutput.map { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME) }
        return variableNames.filterNot { it.isNullOrBlank() }
    }

    /**
     * Extracts parent-scope variables from Call Activity in/out mappings:
     * - operaton:in `source` / `sourceExpression`: variables read from the parent and sent to the child
     * - operaton:out `target`: variables written back into the parent after the child completes
     */
    private fun extractCallActivityMappingVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val inElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_IN)
        val outElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_OUT)
        val sourceVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE)
        val sourceExprVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)
        val targetVars = outElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TARGET)
        return sourceVars + sourceExprVars + targetVars
    }

    private fun extractMultiInstanceVariables(
        nodes: Collection<FlowNode>
    ): List<String> {
        val loops = nodes.flatMap { it.getChildElementsByType(MultiInstanceLoopCharacteristics::class.java) }
        val elementVariables = loops.extractVariablesFromLoops(BpmnModelConstants.CAMUNDA_ATTRIBUTE_ELEMENT_VARIABLE)
        val collectionVariables = loops.extractVariablesFromLoops(BpmnModelConstants.CAMUNDA_ATTRIBUTE_COLLECTION)
        val allVariables = elementVariables + collectionVariables
        return allVariables
    }

    private fun List<MultiInstanceLoopCharacteristics>.extractVariablesFromLoops(
        variableType: String
    ): List<String> {
        return this.mapNotNull { it.getAttributeValueNs(NAMESPACE, variableType) }
    }

}
