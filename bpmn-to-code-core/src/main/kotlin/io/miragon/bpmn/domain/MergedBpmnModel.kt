package io.miragon.bpmn.domain

import io.miragon.bpmn.domain.shared.CallActivityDefinition
import io.miragon.bpmn.domain.shared.CompensationDefinition
import io.miragon.bpmn.domain.shared.ErrorDefinition
import io.miragon.bpmn.domain.shared.EscalationDefinition
import io.miragon.bpmn.domain.shared.FlowNodeDefinition
import io.miragon.bpmn.domain.shared.FlowNodeProperties
import io.miragon.bpmn.domain.shared.MessageDefinition
import io.miragon.bpmn.domain.shared.SequenceFlowDefinition
import io.miragon.bpmn.domain.shared.ServiceTaskDefinition
import io.miragon.bpmn.domain.shared.SignalDefinition
import io.miragon.bpmn.domain.shared.TimerDefinition
import io.miragon.bpmn.domain.shared.VariableDefinition

data class MergedBpmnModel(
    override val processId: String,
    override val flowNodes: List<FlowNodeDefinition>,
    override val messages: List<MessageDefinition>,
    override val signals: List<SignalDefinition>,
    override val errors: List<ErrorDefinition>,
    override val escalations: List<EscalationDefinition> = emptyList(),
    override val compensations: List<CompensationDefinition> = emptyList(),
    val variants: List<VariantData> = emptyList(),
) : ProcessModel {

    override val sequenceFlows: List<SequenceFlowDefinition>
        get() = emptyList()

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

    data class VariantData(
        val variantName: String,
        val sequenceFlows: List<SequenceFlowDefinition>,
        val flowNodes: List<FlowNodeDefinition>,
    )
}
