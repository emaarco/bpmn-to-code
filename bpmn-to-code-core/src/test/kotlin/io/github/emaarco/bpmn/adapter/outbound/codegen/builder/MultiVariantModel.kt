package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.VariantData
import io.github.emaarco.bpmn.domain.shared.*

internal fun buildMultiVariantModel(): MergedBpmnModel {
    val sharedFlowNodes = listOf(
        FlowNodeDefinition(
            id = "StartEvent_OrderReceived",
            elementType = BpmnElementType.START_EVENT,
            outgoing = listOf("Gateway_Route"),
        ),
        FlowNodeDefinition(
            id = "Gateway_Route",
            elementType = BpmnElementType.EXCLUSIVE_GATEWAY,
            incoming = listOf("StartEvent_OrderReceived"),
            outgoing = listOf("Task_ProcessDE", "Task_ProcessAT"),
        ),
        FlowNodeDefinition(
            id = "Task_ProcessDE",
            elementType = BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(
                ServiceTaskDefinition("Task_ProcessDE", engineSpecificProperties = mapOf(ServiceTaskDefinition.IMPL_VALUE_KEY to "order.processDE")),
            ),
            incoming = listOf("Gateway_Route"),
            outgoing = listOf("EndEvent_OrderCompleted"),
        ),
        FlowNodeDefinition(
            id = "Task_ProcessAT",
            elementType = BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(
                ServiceTaskDefinition("Task_ProcessAT", engineSpecificProperties = mapOf(ServiceTaskDefinition.IMPL_VALUE_KEY to "order.processAT")),
            ),
            incoming = listOf("Gateway_Route"),
            outgoing = listOf("EndEvent_OrderCompleted"),
        ),
        FlowNodeDefinition(
            id = "EndEvent_OrderCompleted",
            elementType = BpmnElementType.END_EVENT,
            incoming = listOf("Task_ProcessDE", "Task_ProcessAT"),
        ),
    )

    val deFlowNodes = listOf(
        FlowNodeDefinition(id = "StartEvent_OrderReceived", elementType = BpmnElementType.START_EVENT, outgoing = listOf("Gateway_Route")),
        FlowNodeDefinition(id = "Gateway_Route", elementType = BpmnElementType.EXCLUSIVE_GATEWAY, incoming = listOf("StartEvent_OrderReceived"), outgoing = listOf("Task_ProcessDE")),
        FlowNodeDefinition(id = "Task_ProcessDE", elementType = BpmnElementType.SERVICE_TASK, incoming = listOf("Gateway_Route"), outgoing = listOf("EndEvent_OrderCompleted")),
        FlowNodeDefinition(id = "EndEvent_OrderCompleted", elementType = BpmnElementType.END_EVENT, incoming = listOf("Task_ProcessDE")),
    )

    val atFlowNodes = listOf(
        FlowNodeDefinition(id = "StartEvent_OrderReceived", elementType = BpmnElementType.START_EVENT, outgoing = listOf("Gateway_Route")),
        FlowNodeDefinition(id = "Gateway_Route", elementType = BpmnElementType.EXCLUSIVE_GATEWAY, incoming = listOf("StartEvent_OrderReceived"), outgoing = listOf("Task_ProcessAT")),
        FlowNodeDefinition(id = "Task_ProcessAT", elementType = BpmnElementType.SERVICE_TASK, incoming = listOf("Gateway_Route"), outgoing = listOf("EndEvent_OrderCompleted")),
        FlowNodeDefinition(id = "EndEvent_OrderCompleted", elementType = BpmnElementType.END_EVENT, incoming = listOf("Task_ProcessAT")),
    )

    return MergedBpmnModel(
        processId = "order-process",
        flowNodes = sharedFlowNodes,
        messages = listOf(MessageDefinition("StartEvent_OrderReceived", "Message_OrderReceived")),
        signals = emptyList(),
        errors = emptyList(),
        variants = listOf(
            VariantData(
                variantName = "prodDe",
                sequenceFlows = listOf(
                    SequenceFlowDefinition("Flow_Start_DE", "StartEvent_OrderReceived", "Gateway_Route"),
                    SequenceFlowDefinition("Flow_Route_DE", "Gateway_Route", "Task_ProcessDE", conditionExpression = "country == 'DE'"),
                    SequenceFlowDefinition("Flow_End_DE", "Task_ProcessDE", "EndEvent_OrderCompleted"),
                ),
                flowNodes = deFlowNodes,
            ),
            VariantData(
                variantName = "prodAt",
                sequenceFlows = listOf(
                    SequenceFlowDefinition("Flow_Start_AT", "StartEvent_OrderReceived", "Gateway_Route"),
                    SequenceFlowDefinition("Flow_Route_AT", "Gateway_Route", "Task_ProcessAT", conditionExpression = "country == 'AT'"),
                    SequenceFlowDefinition("Flow_End_AT", "Task_ProcessAT", "EndEvent_OrderCompleted"),
                ),
                flowNodes = atFlowNodes,
            ),
        ),
    )
}
