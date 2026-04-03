package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.constants.CamundaModelConstants
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElements
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.DomElementUtils.withAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.DomElementUtils.withElementName
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

class Camunda7ModelExtractor : EngineSpecificExtractor {

    override fun extract(inputStream: InputStream): BpmnModel {
        val modelInstance = Bpmn.readModelFromStream(inputStream)
        val processId = modelInstance.getProcessId()
        val allMessages = modelInstance.findMessages()
        val allFlowNodes = modelInstance.findFlowNodes()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        val callActivities = findCallActivities(modelInstance)
        val messageSendEvents = findMessageSendEvents(modelInstance)
        val signals = modelInstance.findSignalEventDefinitions()
        val errors = modelInstance.findErrorEventDefinition()
        val timers = modelInstance.findTimerEventDefinition()
        val variables = extractVariables(modelInstance)
        return BpmnModel(
            processId = processId,
            flowNodes = allFlowNodes,
            callActivities = callActivities,
            serviceTasks = serviceTasks + messageSendEvents,
            messages = allMessages,
            signals = signals,
            errors = errors,
            timers = timers,
            variables = variables
        )
    }

    private fun findCallActivities(modelInstance: ModelInstance): List<CallActivityDefinition> {
        val callActivities = modelInstance.getModelElementsByType(CallActivity::class.java)
        return callActivities.mapNotNull {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID) ?: return@mapNotNull null
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
        val camundaTopic = this.detectWorkerType()
        return ServiceTaskDefinition(id = taskId, type = camundaTopic)
    }

    private fun ServiceTask.detectWorkerType(): String? {
        return when {
            this.camundaTopic != null -> this.camundaTopic
            this.camundaDelegateExpression != null -> this.camundaDelegateExpression
            this.camundaClass != null -> this.camundaClass
            else -> null
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
        return when {
            this.camundaTopic != null -> this.camundaTopic to this
            this.camundaDelegateExpression != null -> this.camundaDelegateExpression to this
            this.camundaClass != null -> this.camundaClass to this
            else -> null
        }
    }

    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val allExtensions = flowNodes.flatMap { it.findExtensionElements() }
        val ioExtensions = allExtensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT)
        val ioVariableNames = extractInputAndOutputVariables(ioExtensions)
        val multiInstanceVariableNames = extractMultiInstanceVariables(flowNodes)
        val callActivityMappingVars = extractCallActivityMappingVariables(allExtensions)
        val propertiesExtensions = allExtensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTIES)
        val additionalVariableNames = extractAdditionalVariables(propertiesExtensions)
        val allVariableNames = ioVariableNames + multiInstanceVariableNames + callActivityMappingVars + additionalVariableNames
        return allVariableNames.map { it.removeExpressionSyntax() }.distinct().map { VariableDefinition(it) }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val ioElements = allChildElements.withElementName(*CamundaModelConstants.inputOutputParameters.toTypedArray())
        val variableNames = ioElements.map { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME) }
        return variableNames.filterNot { it.isNullOrBlank() }
    }

    private fun extractAdditionalVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val propertyElements = allChildElements.withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTY)
        val filter = BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME to CamundaModelConstants.ADDITIONAL_VARIABLES_PROPERTY_NAME
        val matchingProperties = propertyElements.withAttribute(filter)
        val commaSeparatedValues = matchingProperties.mapNotNull { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_VALUE) }
        return commaSeparatedValues.flatMap { it.split(",") }.map { it.trim() }.filter { it.isNotBlank() }
    }


    /**
     * Extracts parent-scope variables from Call Activity in/out mappings:
     * - camunda:in `source` / `sourceExpression`: variables read from the parent and sent to the child
     * - camunda:out `target`: variables written back into the parent after the child completes
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
        val elementVariables = loops.mapNotNull { it.camundaElementVariable }
        val collectionVariables = loops.mapNotNull { it.camundaCollection }
        val allVariables = elementVariables + collectionVariables
        return allVariables
    }

}
