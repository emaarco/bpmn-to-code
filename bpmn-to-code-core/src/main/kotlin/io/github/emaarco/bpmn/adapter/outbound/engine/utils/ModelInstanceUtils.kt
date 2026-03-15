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
        val process = this.getModelElementsByType(processType).firstOrNull()
        val processId = process?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
        requireNotNull(process) { "BPMN model does not contain a Process element" }
        requireNotNull(processId) { "Process element is missing an 'id' attribute" }
        return processId
    }

    fun ModelInstance.findFlowNodes(): List<FlowNodeDefinition> {
        val flowNodes = this.getModelElementsByType(FlowNode::class.java)
        return flowNodes.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            requireNotNull(id) { "FlowNode is missing an 'id' attribute" }
            FlowNodeDefinition(id)
        }
    }

    fun ModelInstance.findMessages(): List<MessageDefinition> {
        val messages = this.getModelElementsByType(Message::class.java)
        return messages.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val name = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)
            requireNotNull(name) { "Message element (id: $id) is missing a 'name' attribute" }
            MessageDefinition(name, name)
        }
    }

    fun ModelInstance.findErrorEventDefinition(): List<ErrorDefinition> {
        val errorEvents = this.getModelElementsByType(ErrorEventDefinition::class.java)
        val configuredErrors = errorEvents.mapNotNull { it.error }
        return configuredErrors.map {
            requireNotNull(it.errorCode) { "ErrorEventDefinition is missing an error code" }
            requireNotNull(it.name) { "Error element (id: ${it.id}) is missing a 'name' attribute" }
            ErrorDefinition(id = it.name, name = it.name, code = it.errorCode)
        }
    }

    fun ModelInstance.findSignalEventDefinitions(): List<SignalDefinition> {
        val signalEvents = this.getModelElementsByType(SignalEventDefinition::class.java)
        return signalEvents.map {
            val signal = it.signal
            val name = signal?.name
            requireNotNull(signal) { "SignalEventDefinition is missing a signal reference" }
            requireNotNull(name) { "Signal element (id: ${signal.id}) is missing a 'name' attribute" }
            SignalDefinition(id = name)
        }
    }

    fun ModelInstance.findTimerEventDefinition(): List<TimerDefinition> {
        val timerEvents = this.getModelElementsByType(TimerEventDefinition::class.java)
        return timerEvents.map {
            val timerId = it.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            requireNotNull(timerId) { "The element the TimerEventDefinition belongs to has no 'id' defined" }
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
            error("Timer event definition has no valid type")
        }
    }

}
