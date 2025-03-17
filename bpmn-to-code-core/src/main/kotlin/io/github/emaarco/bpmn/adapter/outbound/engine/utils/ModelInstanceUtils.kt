package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import io.github.emaarco.bpmn.domain.shared.*
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.xml.ModelInstance

object ModelInstanceUtils {

    fun ModelInstance.getProcessId(): String {
        val processType = this.model.getType(Process::class.java)
        return this.getModelElementsByType(processType).first()
            .getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
    }

    fun ModelInstance.findFlowNodes(): List<FlowNodeDefinition> {
        val flowNodes = this.getModelElementsByType(FlowNode::class.java)
        return flowNodes.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            FlowNodeDefinition(id)
        }
    }

    fun ModelInstance.findMessages(): List<MessageDefinition> {
        val messages = this.getModelElementsByType(Message::class.java)
        return messages.map {
            val name = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)
            MessageDefinition(name, name)
        }
    }

    fun ModelInstance.findErrorEventDefinition(): List<ErrorDefinition> {
        val errorEvents = this.getModelElementsByType(ErrorEventDefinition::class.java)
        return errorEvents.map {
            ErrorDefinition(id = it.error.name, name = it.error.name, code = it.error.errorCode)
        }
    }

    fun ModelInstance.findSignalEventDefinitions(): List<SignalDefinition> {
        val signalEvents = this.getModelElementsByType(SignalEventDefinition::class.java)
        return signalEvents.map { SignalDefinition(id = it.signal.name) }
    }

    fun ModelInstance.findTimerEventDefinition(): List<TimerDefinition> {
        val timerEvents = this.getModelElementsByType(TimerEventDefinition::class.java)
        return timerEvents.map {
            val timerId = it.parentElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val (timerType, timerValue) = detectTimerType(it)
            TimerDefinition(id = timerId, type = timerType, value = timerValue)
        }
    }

    private fun ModelInstance.detectTimerType(eventDefinition: TimerEventDefinition): Pair<String, String> {
        return if (eventDefinition.timeDate != null) {
            Pair("Date", eventDefinition.timeDate.textContent)
        } else if (eventDefinition.timeDuration != null) {
            Pair("Duration", eventDefinition.timeDuration.textContent)
        } else if (eventDefinition.timeCycle != null) {
            Pair("Cycle", eventDefinition.timeCycle.textContent)
        } else {
            throw IllegalStateException("Timer event definition has no valid type")
        }
    }

}