package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.utils.FlowNodeUtils.findExtensionElementsWithType
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findMessages
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.io.InputStream

class OperatonModelExtractor : EngineSpecificExtractor {

    companion object {
        private const val OPERATON_NS = "http://operaton.org/schema/1.0/bpmn"
        private const val CAMUNDA_NS = "http://camunda.org/schema/1.0/bpmn"
    }

    override fun extract(inputStream: InputStream): BpmnModel {
        val modelInstance = Bpmn.readModelFromStream(inputStream)
        val processId = modelInstance.getProcessId()
        val messages = modelInstance.findMessages()
        val flowNodes = modelInstance.findFlowNodes()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        val messageSendEvents = findMessageSendEvents(modelInstance)
        val signals = modelInstance.findSignalEventDefinitions()
        val errors = modelInstance.findErrorEventDefinition()
        val timers = modelInstance.findTimerEventDefinition()
        val variables = extractVariables(modelInstance)
        return BpmnModel(
            processId = processId,
            flowNodes = flowNodes,
            serviceTasks = serviceTasks + messageSendEvents,
            messages = messages,
            signals = signals,
            errors = errors,
            timers = timers,
            variables = variables
        )
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
        // Try operaton namespace first, then fall back to camunda namespace
        return when {
            this.getAttributeValueNs(OPERATON_NS, "topic") != null ->
                this.getAttributeValueNs(OPERATON_NS, "topic")
            this.getAttributeValueNs(OPERATON_NS, "delegateExpression") != null ->
                this.getAttributeValueNs(OPERATON_NS, "delegateExpression")
            this.getAttributeValueNs(OPERATON_NS, "class") != null ->
                this.getAttributeValueNs(OPERATON_NS, "class")
            // Fallback to camunda namespace for backward compatibility
            this.camundaTopic != null -> this.camundaTopic
            this.camundaDelegateExpression != null -> this.camundaDelegateExpression
            this.camundaClass != null -> this.camundaClass
            else -> throw IllegalStateException("Service task '${this.id}' has no valid worker type")
        }
    }

    private fun findMessageSendEvents(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val messageEvents = modelInstance.getModelElementsByType(MessageEventDefinition::class.java)
        val sendEvents = messageEvents.mapNotNull { it.detectSendEvents() }
        return sendEvents.map { (type, event) ->
            val taskId = event.parentElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            ServiceTaskDefinition(id = taskId, type = type)
        }
    }

    private fun MessageEventDefinition.detectSendEvents(): Pair<String, MessageEventDefinition>? {
        // Try operaton namespace first, then fall back to camunda namespace
        return when {
            this.getAttributeValueNs(OPERATON_NS, "topic") != null ->
                this.getAttributeValueNs(OPERATON_NS, "topic") to this
            this.getAttributeValueNs(OPERATON_NS, "delegateExpression") != null ->
                this.getAttributeValueNs(OPERATON_NS, "delegateExpression") to this
            this.getAttributeValueNs(OPERATON_NS, "class") != null ->
                this.getAttributeValueNs(OPERATON_NS, "class") to this
            // Fallback to camunda namespace for backward compatibility
            this.camundaTopic != null -> this.camundaTopic to this
            this.camundaDelegateExpression != null -> this.camundaDelegateExpression to this
            this.camundaClass != null -> this.camundaClass to this
            else -> null
        }
    }

    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val extensions = flowNodes.flatMap { it.findExtensionElementsWithType(type = "inputOutput") }
        val variableNames = extractInputAndOutputVariables(extensions)
        return variableNames.distinct().map { VariableDefinition(it) }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allowedDefinitions = listOf("inputParameter", "outputParameter")
        val allElementsInContainer = extensions.flatMap { it.domElement.childElements }
        val eitherInputOrOutput = allElementsInContainer.filter { allowedDefinitions.contains(it.localName) }
        val variableNames = eitherInputOrOutput.map { it.getAttribute("name") }
        return variableNames.filterNot { it.isNullOrBlank() }
    }

}
