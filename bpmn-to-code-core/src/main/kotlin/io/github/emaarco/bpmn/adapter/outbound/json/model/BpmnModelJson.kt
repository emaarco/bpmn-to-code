package io.github.emaarco.bpmn.adapter.outbound.json.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BpmnModelJson(
    val processId: String,
    val flowNodes: List<FlowNodeJson>,
    val messages: List<MessageJson>,
    val signals: List<SignalJson>,
    val errors: List<ErrorJson>,
    val escalations: List<EscalationJson> = emptyList(),
    val compensations: List<CompensationJson> = emptyList(),
    val sequenceFlows: List<SequenceFlowJson>,
)

@Serializable
data class FlowNodeJson(
    val id: String,
    val elementType: String,
    val parentId: String? = null,
    val attachedToRef: String? = null,
    val attachedElements: List<String> = emptyList(),
    val incoming: List<String> = emptyList(),
    val outgoing: List<String> = emptyList(),
    val variables: List<String> = emptyList(),
    val properties: FlowNodePropertiesJson? = null,
    val engineSpecificProperties: Map<String, JsonElement> = emptyMap(),
)

@Serializable
data class FlowNodePropertiesJson(
    val type: String,
    val implementationValue: String? = null,
    val implementationKind: String? = null,
    val calledElement: String? = null,
    val timerType: String? = null,
    val timerValue: String? = null,
)

@Serializable
data class SequenceFlowJson(
    val id: String,
    val sourceRef: String,
    val targetRef: String,
    val name: String? = null,
    val conditionExpression: String? = null,
    val isDefault: Boolean,
)

@Serializable
data class MessageJson(
    val id: String,
    val name: String,
)

@Serializable
data class SignalJson(
    val id: String,
    val name: String,
)

@Serializable
data class ErrorJson(
    val id: String,
    val name: String,
    val code: String,
)

@Serializable
data class EscalationJson(
    val id: String,
    val name: String,
    val code: String,
)

@Serializable
data class CompensationJson(
    val id: String,
    val activityRef: String,
)
