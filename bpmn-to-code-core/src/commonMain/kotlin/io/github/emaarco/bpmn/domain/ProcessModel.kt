package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationDefinition
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition

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
