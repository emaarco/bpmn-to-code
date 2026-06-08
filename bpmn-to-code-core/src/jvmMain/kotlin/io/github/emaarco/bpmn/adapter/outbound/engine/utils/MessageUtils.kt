package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import io.github.emaarco.bpmn.adapter.outbound.engine.helpers.MessageSource
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.ReceiveTask
import org.camunda.bpm.model.xml.ModelInstance

object MessageUtils {

    fun ModelInstance.findEventBasedMessagesWithSource(): List<MessageSource> {
        return this.getModelElementsByType(MessageEventDefinition::class.java)
            .mapNotNull { med ->
                val message = med.message ?: return@mapNotNull null
                val elementId = med.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
                val name = message.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)
                MessageSource(elementId, name, message)
            }
    }

    fun ModelInstance.findTaskBasedMessagesWithSource(): List<MessageSource> {
        return this.getModelElementsByType(ReceiveTask::class.java)
            .map { task ->
                val elementId = task.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
                val name = task.message?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)
                MessageSource(elementId, name, task.message)
            }
    }

    fun ModelInstance.findAllMessagesWithSource(): List<MessageSource> =
        findEventBasedMessagesWithSource() + findTaskBasedMessagesWithSource()
}
