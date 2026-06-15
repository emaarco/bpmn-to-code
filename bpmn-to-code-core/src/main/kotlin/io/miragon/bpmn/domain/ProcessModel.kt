package io.miragon.bpmn.domain

import io.miragon.bpmn.domain.shared.CallActivityDefinition
import io.miragon.bpmn.domain.shared.CompensationDefinition
import io.miragon.bpmn.domain.shared.ErrorDefinition
import io.miragon.bpmn.domain.shared.EscalationDefinition
import io.miragon.bpmn.domain.shared.FlowNodeDefinition
import io.miragon.bpmn.domain.shared.MessageDefinition
import io.miragon.bpmn.domain.shared.SequenceFlowDefinition
import io.miragon.bpmn.domain.shared.ServiceTaskDefinition
import io.miragon.bpmn.domain.shared.SignalDefinition
import io.miragon.bpmn.domain.shared.TimerDefinition
import io.miragon.bpmn.domain.shared.VariableDefinition

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
