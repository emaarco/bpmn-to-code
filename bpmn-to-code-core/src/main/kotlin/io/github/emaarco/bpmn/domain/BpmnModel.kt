package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition

data class BpmnModel(
    val processId: String,
    val flowNodes: List<FlowNodeDefinition>,
    val serviceTasks: List<ServiceTaskDefinition>,
    val messages: List<MessageDefinition>,
)
