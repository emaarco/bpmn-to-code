package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.Message
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition
import org.camunda.bpm.model.xml.ModelInstance

/**
 * Utility functions for extracting BPMN elements that are common across process engines.
 * Use this only if you have a method that can be used by multiple extractors.
 */
object ModelInstanceUtils {

    fun ModelInstance.getProcessId(): String {
        val processType = this.model.getType(Process::class.java)
        val processes = this.getModelElementsByType(processType)
        require(processes.isNotEmpty()) { "BPMN file contains no process definition" }
        val id = processes.first().getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
        requireNotNull(id) { "Process element is missing an ID attribute" }
        return id
    }

    fun ModelInstance.findFlowNodes(): List<FlowNodeDefinition> {
        val flowNodes = this.getModelElementsByType(FlowNode::class.java)
        return flowNodes.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            requireNotNull(id) { "Flow node in process '${this.getProcessId()}' is missing an ID attribute" }
            FlowNodeDefinition(id)
        }
    }

    fun ModelInstance.findMessages(): List<MessageDefinition> {
        val messages = this.getModelElementsByType(Message::class.java)
        return messages.map {
            val name = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)
            requireNotNull(name) { "Message element in process '${this.getProcessId()}' is missing a name attribute" }
            MessageDefinition(name, name)
        }
    }

    fun ModelInstance.findErrorEventDefinition(): List<ErrorDefinition> {
        val errorEvents = this.getModelElementsByType(ErrorEventDefinition::class.java)
        val configuredErrors = errorEvents.mapNotNull { it.error }
        return configuredErrors.map {
            val name = it.name
            requireNotNull(name) { "Error element in process '${this.getProcessId()}' is missing a name attribute" }
            val code = it.errorCode
            requireNotNull(code) { "Error '$name' in process '${this.getProcessId()}' is missing an error code" }
            ErrorDefinition(id = name, name = name, code = code)
        }
    }

    fun ModelInstance.findSignalEventDefinitions(): List<SignalDefinition> {
        val signalEvents = this.getModelElementsByType(SignalEventDefinition::class.java)
        return signalEvents.map {
            val signal = it.signal
            requireNotNull(signal) { "Signal event definition in process '${this.getProcessId()}' is missing a signal reference" }
            val name = signal.name
            requireNotNull(name) { "Signal in process '${this.getProcessId()}' is missing a name attribute" }
            SignalDefinition(id = name)
        }
    }

    fun ModelInstance.findTimerEventDefinition(): List<TimerDefinition> {
        val timerEvents = this.getModelElementsByType(TimerEventDefinition::class.java)
        return timerEvents.map {
            val timerId = it.parentElement.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val (timerType, timerValue) = it.detectTimerType(timerId)
            TimerDefinition(id = timerId, type = timerType, value = timerValue)
        }
    }

    private fun TimerEventDefinition.detectTimerType(timerId: String): Pair<String, String> {
        if (this.timeDate != null) return Pair("Date", this.timeDate.textContent)
        if (this.timeDuration != null) return Pair("Duration", this.timeDuration.textContent)
        if (this.timeCycle != null) return Pair("Cycle", this.timeCycle.textContent)
        error("Timer event '$timerId' is missing a valid type (expected timeDate, timeDuration, or timeCycle)")
    }

}