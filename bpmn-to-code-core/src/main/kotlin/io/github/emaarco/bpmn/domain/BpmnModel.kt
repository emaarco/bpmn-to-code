package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.*

data class BpmnModel(
    val processId: String,
    val flowNodes: List<FlowNodeDefinition>,
    val messages: List<MessageDefinition>,
    val signals: List<SignalDefinition>,
    val errors: List<ErrorDefinition>,
) {
    val serviceTasks: List<ServiceTaskDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.ServiceTask)?.definition }

    val callActivities: List<CallActivityDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.CallActivity)?.definition }

    val timers: List<TimerDefinition>
        get() = flowNodes.mapNotNull { (it.properties as? FlowNodeProperties.Timer)?.definition }

    val variables: List<VariableDefinition>
        get() = flowNodes.flatMap { it.variables }.distinct()
}
