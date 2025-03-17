package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.*

data class BpmnModel(
    val processId: String,
    val flowNodes: List<FlowNodeDefinition>,
    val serviceTasks: List<ServiceTaskDefinition>,
    val messages: List<MessageDefinition>,
    val signals: List<SignalDefinition>,
    val errors: List<ErrorDefinition>,
    val timers: List<TimerDefinition>,
)
