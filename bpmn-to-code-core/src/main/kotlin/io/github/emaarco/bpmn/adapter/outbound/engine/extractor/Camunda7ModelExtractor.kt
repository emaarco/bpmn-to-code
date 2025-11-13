package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

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
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import java.io.File

class Camunda7ModelExtractor : EngineSpecificExtractor {

    override fun extract(file: File): BpmnModel {
        val modelInstance = Bpmn.readModelFromFile(file)
        val processId = modelInstance.getProcessId()
        val messages = modelInstance.findMessages()
        val flowNodes = modelInstance.findFlowNodes()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        val messageSendEvents = findMessageSendEvents(modelInstance)
        val signals = modelInstance.findSignalEventDefinitions()
        val errors = modelInstance.findErrorEventDefinition()
        val timers = modelInstance.findTimerEventDefinition()
        val variables = extractVariables()
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

    private fun extractVariables(): List<VariableDefinition> {
        // TODO: Extract variables from camunda:inputParameter/@name and camunda:outputParameter/@name
        // Challenge: Accessing nested child elements through the Camunda BPMN Model API
        return listOf(VariableDefinition("subscriptionId"))
    }

}
