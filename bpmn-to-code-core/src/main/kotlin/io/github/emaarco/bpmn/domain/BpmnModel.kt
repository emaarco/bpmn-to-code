package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.*

data class BpmnModel(
    val processId: String,
    val flowNodes: List<FlowNodeDefinition>,
    val sequenceFlows: List<SequenceFlowDefinition> = emptyList(),
    val messages: List<MessageDefinition>,
    val signals: List<SignalDefinition>,
    val errors: List<ErrorDefinition>,
    val escalations: List<EscalationDefinition> = emptyList(),
) {
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
}
