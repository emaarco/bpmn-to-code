package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.adapter.outbound.json.model.*
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.*

class BpmnJsonMapper {

    fun toJson(model: BpmnModel): BpmnModelJson {
        return BpmnModelJson(
            processId = model.processId,
            flowNodes = model.flowNodes.map { it.toJson() },
            sequenceFlows = model.sequenceFlows.map { it.toJson() },
            messages = model.messages.mapNotNull { it.toJson() },
            signals = model.signals.mapNotNull { it.toJson() },
            errors = model.errors.mapNotNull { it.toJson() },
        )
    }

    private fun FlowNodeDefinition.toJson(): FlowNodeJson {
        return FlowNodeJson(
            id = id ?: "",
            elementType = elementType.name,
            parentId = parentId,
            attachedToRef = attachedToRef,
            incoming = incoming,
            outgoing = outgoing,
            variables = variables.map { it.getRawName() },
            properties = properties.toJson(),
        )
    }

    private fun FlowNodeProperties.toJson(): FlowNodePropertiesJson? = when (this) {
        is FlowNodeProperties.None -> null
        is FlowNodeProperties.ServiceTask -> FlowNodePropertiesJson(
            type = "ServiceTask",
            implementationValue = definition.customProperties[ServiceTaskDefinition.IMPL_VALUE_KEY] as? String,
            implementationKind = definition.customProperties[ServiceTaskDefinition.IMPL_KIND_KEY] as? String,
        )
        is FlowNodeProperties.CallActivity -> FlowNodePropertiesJson(
            type = "CallActivity",
            calledElement = definition.getValue(),
        )
        is FlowNodeProperties.Timer -> {
            val (type, value) = definition.getValue()
            FlowNodePropertiesJson(
                type = "Timer",
                timerType = type.takeIf { it.isNotEmpty() },
                timerValue = value.takeIf { it.isNotEmpty() },
            )
        }
    }

    private fun SequenceFlowDefinition.toJson(): SequenceFlowJson {
        return SequenceFlowJson(
            id = id ?: "",
            sourceRef = sourceRef,
            targetRef = targetRef,
            name = flowName,
            conditionExpression = conditionExpression,
        )
    }

    private fun MessageDefinition.toJson(): MessageJson? {
        val name = getValue().takeIf { it.isNotEmpty() } ?: return null
        return MessageJson(id = id ?: "", name = name)
    }

    private fun SignalDefinition.toJson(): SignalJson? {
        val name = getValue().takeIf { it.isNotEmpty() } ?: return null
        return SignalJson(id = id ?: "", name = name)
    }

    private fun ErrorDefinition.toJson(): ErrorJson? {
        val (name, code) = getValue()
        if (name.isEmpty()) return null
        return ErrorJson(id = id ?: "", name = name, code = code)
    }
}
