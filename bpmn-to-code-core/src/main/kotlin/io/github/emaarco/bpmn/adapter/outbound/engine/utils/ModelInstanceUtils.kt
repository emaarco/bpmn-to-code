package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.Message
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.xml.ModelInstance

object ModelInstanceUtils {

    fun ModelInstance.getProcessId(): String {
        val processType = this.model.getType(Process::class.java)
        return this.getModelElementsByType(processType).first()
            .getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
    }

    fun ModelInstance.getFlowNodes(): List<FlowNodeDefinition> {
        val flowNodes = this.getModelElementsByType(FlowNode::class.java)
        return flowNodes.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            FlowNodeDefinition(id)
        }
    }

    fun ModelInstance.getMessages(): List<MessageDefinition> {
        val messages = this.getModelElementsByType(Message::class.java)
        return messages.map {
            val name = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)
            MessageDefinition(name, name)
        }
    }

}