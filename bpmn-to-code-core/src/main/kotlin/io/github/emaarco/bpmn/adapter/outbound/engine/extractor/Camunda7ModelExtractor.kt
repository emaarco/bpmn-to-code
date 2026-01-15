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
import io.github.emaarco.bpmn.domain.utils.StringUtils.removeExpressionSyntax
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
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
        val camundaTopic = this.detectWorkerType()
        return ServiceTaskDefinition(id = taskId, type = camundaTopic)
    }

    private fun ServiceTask.detectWorkerType(): String {
        return when {
            this.camundaTopic != null -> this.camundaTopic
            this.camundaDelegateExpression != null -> this.camundaDelegateExpression
            this.camundaClass != null -> this.camundaClass
            else -> throw IllegalStateException("Service task '${this.id}' has no worker valid type")
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
        return when {
            this.camundaTopic != null -> this.camundaTopic to this
            this.camundaDelegateExpression != null -> this.camundaDelegateExpression to this
            this.camundaClass != null -> this.camundaClass to this
            else -> null
        }
    }

    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val extensions = flowNodes.flatMap { it.findExtensionElementsWithType(type = "inputOutput") }
        val ioVariableNames = extractInputAndOutputVariables(extensions)
        val multiInstanceVariableNames = extractMultiInstanceVariables(flowNodes)
        val allVariableNames = ioVariableNames + multiInstanceVariableNames
        return allVariableNames.distinct().map { VariableDefinition(it) }
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

    private fun extractMultiInstanceVariables(
        nodes: Collection<FlowNode>
    ): List<String> {
        val loops = nodes.flatMap { it.getChildElementsByType(MultiInstanceLoopCharacteristics::class.java) }
        val elementVariables = loops.map { it.camundaElementVariable }
        val collectionVariables = loops.map { it.camundaCollection }
        val allVariables = elementVariables + collectionVariables
        return allVariables.map { it.removeExpressionSyntax() }
    }

}
