package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.*

sealed interface ProcessModel {
    val processId: String
    val flowNodes: List<FlowNodeDefinition>
    val sequenceFlows: List<SequenceFlowDefinition>
    val messages: List<MessageDefinition>
    val signals: List<SignalDefinition>
    val errors: List<ErrorDefinition>
    val escalations: List<EscalationDefinition>
    val compensations: List<CompensationDefinition>
    val serviceTasks: List<ServiceTaskDefinition>
    val callActivities: List<CallActivityDefinition>
    val timers: List<TimerDefinition>
    val variables: List<VariableDefinition>
}
