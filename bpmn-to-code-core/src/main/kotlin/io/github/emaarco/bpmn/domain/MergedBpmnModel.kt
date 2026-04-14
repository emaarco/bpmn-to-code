package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.*

data class VariantData(
    val variantName: String,
    val sequenceFlows: List<SequenceFlowDefinition>,
    val flowNodes: List<FlowNodeDefinition>,
)

data class MergedBpmnModel(
    val processId: String,
    val flowNodes: List<FlowNodeDefinition>,
    val messages: List<MessageDefinition>,
    val signals: List<SignalDefinition>,
    val errors: List<ErrorDefinition>,
    val escalations: List<EscalationDefinition> = emptyList(),
    val compensations: List<CompensationDefinition> = emptyList(),
    val variants: List<VariantData> = emptyList(),
) {
    val isMultiVariant: Boolean get() = variants.size > 1

    val sequenceFlows: List<SequenceFlowDefinition>
        get() {
            return if (!isMultiVariant) variants.firstOrNull()?.sequenceFlows.orEmpty() else emptyList()
        }

    val serviceTasks: List<ServiceTaskDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.ServiceTask)?.definition }
            .sortedBy { it.getRawName() }

    val callActivities: List<CallActivityDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.CallActivity)?.definition }
            .sortedBy { it.getRawName() }

    val timers: List<TimerDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.Timer)?.definition }
            .sortedBy { it.getRawName() }

    val variables: List<VariableDefinition>
        get() = flowNodes.flatMap { it.variables }.distinct()
            .sortedBy { it.getRawName() }

    fun toFlatModel(): BpmnModel {
        val allSequenceFlows = variants.flatMap { it.sequenceFlows }
            .distinctBy { it.getRawName() }
        return BpmnModel(
            processId = processId,
            flowNodes = flowNodes,
            sequenceFlows = allSequenceFlows,
            messages = messages,
            signals = signals,
            errors = errors,
            escalations = escalations,
            compensations = compensations,
        )
    }
}
