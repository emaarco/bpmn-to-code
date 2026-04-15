package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition

internal fun buildSubscribeNewsletterFlowNodes(
    confirmationMailImpl: String,
    welcomeMailImpl: String,
    registrationCompletedImpl: String,
    extraVariables: List<VariableDefinition> = emptyList(),
) = listOf(
    FlowNodeDefinition(
        id = "CallActivity_AbortRegistration",
        elementType = BpmnElementType.CALL_ACTIVITY,
        properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
        variables = listOf(VariableDefinition("subscriptionId")),
        incoming = listOf("Timer_After3Days"),
        outgoing = listOf("CompensationEndEvent_RegistrationAborted"),
    ),
    FlowNodeDefinition(
        id = "Activity_ConfirmRegistration",
        elementType = BpmnElementType.RECEIVE_TASK,
        attachedElements = listOf("Timer_EveryDay"),
        parentId = "SubProcess_Confirmation",
        incoming = listOf("Activity_SendConfirmationMail"),
        outgoing = listOf("EndEvent_SubscriptionConfirmed"),
    ),
    FlowNodeDefinition(
        id = "Activity_SendConfirmationMail",
        elementType = BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendConfirmationMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to confirmationMailImpl))),
        variables = listOf(VariableDefinition("subscriptionId")) + extraVariables,
        parentId = "SubProcess_Confirmation",
        incoming = listOf("StartEvent_RequestReceived", "Timer_EveryDay"),
        outgoing = listOf("Activity_ConfirmRegistration"),
    ),
    FlowNodeDefinition(
        id = "Activity_SendWelcomeMail",
        elementType = BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendWelcomeMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to welcomeMailImpl))),
        variables = listOf(VariableDefinition("subscriptionId")),
        incoming = listOf("SubProcess_Confirmation"),
        outgoing = listOf("EndEvent_RegistrationCompleted"),
    ),
    FlowNodeDefinition(
        id = "CompensationEndEvent_RegistrationAborted",
        elementType = BpmnElementType.END_EVENT,
        incoming = listOf("CallActivity_AbortRegistration"),
    ),
    FlowNodeDefinition(
        id = "CompensationEvent_OnSubscriptionCounter",
        elementType = BpmnElementType.BOUNDARY_EVENT,
        attachedToRef = "serviceTask_incrementSubscriptionCounter",
    ),
    FlowNodeDefinition(
        id = "CompensationTask_DecrementSubscriptionCounter",
        elementType = BpmnElementType.TASK,
    ),
    FlowNodeDefinition(
        id = "EndEvent_RegistrationCompleted",
        elementType = BpmnElementType.END_EVENT,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("EndEvent_RegistrationCompleted", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to registrationCompletedImpl))),
        variables = listOf(VariableDefinition("subscriptionId")),
        incoming = listOf("Activity_SendWelcomeMail"),
    ),
    FlowNodeDefinition(
        id = "EndEvent_RegistrationNotPossible",
        elementType = BpmnElementType.END_EVENT,
        incoming = listOf("ErrorEvent_InvalidMail"),
    ),
    FlowNodeDefinition(
        id = "EndEvent_SubscriptionConfirmed",
        elementType = BpmnElementType.END_EVENT,
        parentId = "SubProcess_Confirmation",
        incoming = listOf("Activity_ConfirmRegistration"),
    ),
    FlowNodeDefinition(
        id = "ErrorEvent_InvalidMail",
        elementType = BpmnElementType.BOUNDARY_EVENT,
        attachedToRef = "SubProcess_Confirmation",
        outgoing = listOf("EndEvent_RegistrationNotPossible"),
    ),
    FlowNodeDefinition(
        id = "serviceTask_incrementSubscriptionCounter",
        elementType = BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_incrementSubscriptionCounter", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "counterClass"))),
        attachedElements = listOf("CompensationEvent_OnSubscriptionCounter"),
        incoming = listOf("StartEvent_SubmitRegistrationForm"),
        outgoing = listOf("SubProcess_Confirmation"),
    ),
    FlowNodeDefinition(
        id = "StartEvent_RequestReceived",
        elementType = BpmnElementType.START_EVENT,
        variables = listOf(VariableDefinition("subscriptionId")),
        parentId = "SubProcess_Confirmation",
        outgoing = listOf("Activity_SendConfirmationMail"),
    ),
    FlowNodeDefinition(
        id = "StartEvent_SubmitRegistrationForm",
        elementType = BpmnElementType.START_EVENT,
        variables = listOf(VariableDefinition("subscriptionId")),
        outgoing = listOf("serviceTask_incrementSubscriptionCounter"),
    ),
    FlowNodeDefinition(
        id = "SubProcess_Confirmation",
        elementType = BpmnElementType.SUB_PROCESS,
        attachedElements = listOf("ErrorEvent_InvalidMail", "Timer_After3Days"),
        incoming = listOf("serviceTask_incrementSubscriptionCounter"),
        outgoing = listOf("Activity_SendWelcomeMail"),
    ),
    FlowNodeDefinition(
        id = "Timer_After3Days",
        elementType = BpmnElementType.BOUNDARY_EVENT,
        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "\${testVariable}")),
        attachedToRef = "SubProcess_Confirmation",
        outgoing = listOf("CallActivity_AbortRegistration"),
    ),
    FlowNodeDefinition(
        id = "Timer_EveryDay",
        elementType = BpmnElementType.BOUNDARY_EVENT,
        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
        attachedToRef = "Activity_ConfirmRegistration",
        parentId = "SubProcess_Confirmation",
        outgoing = listOf("Activity_SendConfirmationMail"),
    ),
)
