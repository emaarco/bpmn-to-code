package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.bpmn.instance.SequenceFlow
import org.camunda.bpm.model.bpmn.instance.SubProcess
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
            val elementType = BpmnElementType.fromTypeName(it.elementType.typeName)
            val attachedToRef = if (it is BoundaryEvent) it.attachedTo?.id else null
            val parentId = (it.parentElement as? SubProcess)?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val incoming = it.incoming.mapNotNull { flow -> flow.source?.id }
            val outgoing = it.outgoing.mapNotNull { flow -> flow.target?.id }
            FlowNodeDefinition(
                id = id,
                elementType = elementType,
                attachedToRef = attachedToRef,
                parentId = parentId,
                incoming = incoming,
                outgoing = outgoing,
            )
        }
    }

    fun ModelInstance.findSequenceFlows(): List<SequenceFlowDefinition> {
        val sequenceFlows = this.getModelElementsByType(SequenceFlow::class.java)
        return sequenceFlows.mapNotNull { flow ->
            val id = flow.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val sourceRef = flow.source?.id ?: return@mapNotNull null
            val targetRef = flow.target?.id ?: return@mapNotNull null
            val flowName = flow.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_NAME)?.takeIf { it.isNotBlank() }
            val condition = flow.conditionExpression?.textContent?.takeIf { it.isNotBlank() }
            SequenceFlowDefinition(
                id = id,
                sourceRef = sourceRef,
                targetRef = targetRef,
                flowName = flowName,
                conditionExpression = condition,
            )
        }
    }

    fun ModelInstance.findErrorEventDefinition(): List<ErrorDefinition> {
        val errorEvents = this.getModelElementsByType(ErrorEventDefinition::class.java)
        return errorEvents.map {
            val elementId = it.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            ErrorDefinition(id = elementId, name = it.error?.name, code = it.error?.errorCode)
        }
    }

    fun ModelInstance.findSignalEventDefinitions(): List<SignalDefinition> {
        val signalEvents = this.getModelElementsByType(SignalEventDefinition::class.java)
        return signalEvents.map {
            val elementId = it.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val name = it.signal?.name
            SignalDefinition(id = elementId, name = name)
        }
    }

    fun ModelInstance.findTimerEventDefinition(): List<TimerDefinition> {
        val timerEvents = this.getModelElementsByType(TimerEventDefinition::class.java)
        return timerEvents.map {
            val timerId = it.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val timerTypeValue = it.detectTimerType()
            TimerDefinition(id = timerId, type = timerTypeValue?.first, value = timerTypeValue?.second)
        }
    }

    private fun TimerEventDefinition.detectTimerType(): Pair<String, String>? {
        return if (this.timeDate != null) {
            Pair("Date", this.timeDate.textContent)
        } else if (this.timeDuration != null) {
            Pair("Duration", this.timeDuration.textContent)
        } else if (this.timeCycle != null) {
            Pair("Cycle", this.timeCycle.textContent)
        } else {
            null
        }
    }

}
