package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.adapter.outbound.json.model.*
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.ProcessModel
import io.github.emaarco.bpmn.domain.shared.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class BpmnJsonMapper {

    fun toJson(model: ProcessModel): BpmnModelJson {
        return when (model) {
            is BpmnModel -> toFlatJson(model)
            is MergedBpmnModel -> toVariantJson(model)
        }
    }

    private fun toFlatJson(model: BpmnModel): BpmnModelJson {
        return BpmnModelJson(
            processId = model.processId,
            flowNodes = FlowNodeSorter.sort(model.flowNodes).map { it.toJson() },
            sequenceFlows = model.sequenceFlows.map { it.toJson() },
            messages = model.messages.mapNotNull { it.toJson() },
            signals = model.signals.mapNotNull { it.toJson() },
            errors = model.errors.mapNotNull { it.toJson() },
            escalations = model.escalations.mapNotNull { it.toJson() },
            compensations = model.compensations.mapNotNull { it.toJson() },
        )
    }

    private fun toVariantJson(model: MergedBpmnModel): BpmnModelJson {
        return BpmnModelJson(
            processId = model.processId,
            flowNodes = FlowNodeSorter.sort(model.flowNodes).map { it.toJson() },
            messages = model.messages.mapNotNull { it.toJson() },
            signals = model.signals.mapNotNull { it.toJson() },
            errors = model.errors.mapNotNull { it.toJson() },
            escalations = model.escalations.mapNotNull { it.toJson() },
            compensations = model.compensations.mapNotNull { it.toJson() },
            variants = model.variants.map { variant ->
                VariantJson(
                    variantName = variant.variantName,
                    flowNodes = FlowNodeSorter.sort(variant.flowNodes).map { it.toJson() },
                    sequenceFlows = variant.sequenceFlows.map { it.toJson() },
                )
            },
        )
    }

    private fun FlowNodeDefinition.toJson(): FlowNodeJson {
        return FlowNodeJson(
            id = id ?: "",
            elementType = elementType.name,
            parentId = parentId,
            attachedToRef = attachedToRef,
            attachedElements = attachedElements,
            incoming = incoming,
            outgoing = outgoing,
            variables = variables.map { it.getRawName() },
            properties = properties.toJson(),
            engineSpecificProperties = engineSpecificProperties.mapValues { (_, v) -> v.toJsonElement() },
        )
    }

    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        else -> JsonPrimitive(this.toString())
    }

    private fun FlowNodeProperties.toJson(): FlowNodePropertiesJson? = when (this) {
        is FlowNodeProperties.None -> null
        is FlowNodeProperties.ServiceTask -> FlowNodePropertiesJson(
            type = "ServiceTask",
            implementationValue = definition.engineSpecificProperties[ServiceTaskDefinition.IMPL_VALUE_KEY] as? String,
            implementationKind = definition.engineSpecificProperties[ServiceTaskDefinition.IMPL_KIND_KEY] as? String,
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
            isDefault = isDefault,
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

    private fun EscalationDefinition.toJson(): EscalationJson? {
        val (name, code) = getValue()
        if (name.isEmpty()) return null
        return EscalationJson(id = id ?: "", name = name, code = code)
    }

    private fun CompensationDefinition.toJson(): CompensationJson? {
        val activityRef = getValue().takeIf { it.isNotEmpty() } ?: return null
        return CompensationJson(id = id ?: "", activityRef = activityRef)
    }
}
