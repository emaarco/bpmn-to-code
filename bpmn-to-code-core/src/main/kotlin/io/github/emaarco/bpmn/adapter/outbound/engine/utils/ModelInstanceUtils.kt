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
        val configuredErrors = errorEvents.mapNotNull { it.error }
        return configuredErrors.map {
            ErrorDefinition(id = it.name, name = it.name, code = it.errorCode)
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
            val (timerType, timerValue) = it.detectTimerType()
            TimerDefinition(id = timerId, type = timerType, value = timerValue)
        }
    }

    private fun TimerEventDefinition.detectTimerType(): Pair<String, String> {
        return if (this.timeDate != null) {
            Pair("Date", this.timeDate.textContent)
        } else if (this.timeDuration != null) {
            Pair("Duration", this.timeDuration.textContent)
        } else if (this.timeCycle != null) {
            Pair("Cycle", this.timeCycle.textContent)
        } else {
            throw IllegalStateException("Timer event definition has no valid type")
        }
    }

}