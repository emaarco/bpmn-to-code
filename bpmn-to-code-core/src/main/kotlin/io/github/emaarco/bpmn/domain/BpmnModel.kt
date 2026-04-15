package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.*

data class BpmnModel(
    override val processId: String,
    val variantName: String? = null,
    override val flowNodes: List<FlowNodeDefinition>,
    override val sequenceFlows: List<SequenceFlowDefinition> = emptyList(),
    override val messages: List<MessageDefinition>,
    override val signals: List<SignalDefinition>,
    override val errors: List<ErrorDefinition>,
    override val escalations: List<EscalationDefinition> = emptyList(),
    override val compensations: List<CompensationDefinition> = emptyList(),
) : ProcessModel {
    override val serviceTasks: List<ServiceTaskDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.ServiceTask)?.definition }
            .distinctBy { it.getRawName() }
            .sortedBy { it.getRawName() }

    override val callActivities: List<CallActivityDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.CallActivity)?.definition }
            .sortedBy { it.getRawName() }

    override val timers: List<TimerDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.Timer)?.definition }
            .sortedBy { it.getRawName() }

    override val variables: List<VariableDefinition>
        get() = flowNodes.flatMap { it.variables }.distinct()
            .sortedBy { it.getRawName() }
}
