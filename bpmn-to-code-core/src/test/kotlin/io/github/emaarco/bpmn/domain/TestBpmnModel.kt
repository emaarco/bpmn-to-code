package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationDefinition
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_AFTER_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_BEFORE_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.EXCLUSIVE_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition

fun testBpmnModel(
    processId: String = "order",
    variantName: String? = null,
    flowNodes: List<FlowNodeDefinition> = listOf(FlowNodeDefinition(id = "create-order")),
    sequenceFlows: List<SequenceFlowDefinition> = emptyList(),
    messages: List<MessageDefinition> = listOf(MessageDefinition(id = "messageId", name = "messageName")),
    signals: List<SignalDefinition> = listOf(SignalDefinition(id = "signalId", name = "signalName")),
    errors: List<ErrorDefinition> = listOf(ErrorDefinition(id = "errorId", name = "errorName", code = "errorCode")),
    escalations: List<EscalationDefinition> = emptyList(),
    compensations: List<CompensationDefinition> = emptyList(),
) = BpmnModel(
    processId = processId,
    variantName = variantName,
    flowNodes = flowNodes,
    sequenceFlows = sequenceFlows,
    messages = messages,
    signals = signals,
    errors = errors,
    escalations = escalations,
    compensations = compensations,
)

fun testMergedBpmnModel(
    model: BpmnModel = testBpmnModel(),
): MergedBpmnModel {
    return MergedBpmnModel(
        processId = model.processId,
        flowNodes = model.flowNodes,
        messages = model.messages,
        signals = model.signals,
        errors = model.errors,
        escalations = model.escalations,
        compensations = model.compensations,
        variants = listOf(
            VariantData(
                variantName = model.variantName ?: model.processId,
                sequenceFlows = model.sequenceFlows,
                flowNodes = model.flowNodes,
            )
        ),
    )
}

fun testBpmnModelApi(
    model: BpmnModel = testBpmnModel(),
    packagePath: String = "packagePath",
    language: OutputLanguage = OutputLanguage.KOTLIN,
    engine: ProcessEngine = ProcessEngine.ZEEBE,
) = BpmnModelApi(
    model = testMergedBpmnModel(model),
    packagePath = packagePath,
    outputLanguage = language,
    engine = engine,
)

fun testNewsletterBpmnModel(
    processId: String = "newsletterSubscription",
    variantName: String? = null,
    testVariableForTimer: String = "$" + "{testVariable}",
    flowNodes: List<FlowNodeDefinition> = listOf(
        FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
            properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
            variables = listOf(VariableDefinition("subscriptionId")),
            incoming = listOf("Timer_After3Days"), outgoing = listOf("EndEvent_RegistrationAborted")),
        FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.USER_TASK,
            attachedElements = listOf("Timer_EveryDay"),
            parentId = "SubProcess_Confirmation",
            incoming = listOf("Activity_SendConfirmationMail"), outgoing = listOf("EndEvent_SubscriptionConfirmed")),
        FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendConfirmationMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail"))),
            variables = listOf(VariableDefinition("subscriptionId")),
            parentId = "SubProcess_Confirmation",
            incoming = listOf("StartEvent_RequestReceived", "Timer_EveryDay"), outgoing = listOf("Activity_ConfirmRegistration")),
        FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendWelcomeMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail"))),
            variables = listOf(VariableDefinition("subscriptionId")),
            incoming = listOf("SubProcess_Confirmation"), outgoing = listOf("EndEvent_RegistrationCompleted"),
            engineSpecificProperties = mapOf(ASYNC_BEFORE_KEY to true, ASYNC_AFTER_KEY to true, EXCLUSIVE_KEY to false)),
        FlowNodeDefinition("EndEvent_RegistrationAborted", BpmnElementType.END_EVENT,
            incoming = listOf("CallActivity_AbortRegistration")),
        FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("EndEvent_RegistrationCompleted", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted"))),
            variables = listOf(VariableDefinition("subscriptionId")),
            incoming = listOf("Activity_SendWelcomeMail")),
        FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT,
            incoming = listOf("ErrorEvent_InvalidMail")),
        FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT,
            parentId = "SubProcess_Confirmation",
            incoming = listOf("Activity_ConfirmRegistration")),
        FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
            attachedToRef = "SubProcess_Confirmation",
            outgoing = listOf("EndEvent_RegistrationNotPossible")),
        FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
            variables = listOf(VariableDefinition("subscriptionId")),
            parentId = "SubProcess_Confirmation",
            outgoing = listOf("Activity_SendConfirmationMail")),
        FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
            variables = listOf(VariableDefinition("subscriptionId")),
            outgoing = listOf("SubProcess_Confirmation")),
        FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS,
            attachedElements = listOf("ErrorEvent_InvalidMail", "Timer_After3Days"),
            incoming = listOf("StartEvent_SubmitRegistrationForm"), outgoing = listOf("Activity_SendWelcomeMail")),
        FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
            properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "$" + "{testVariable}")),
            attachedToRef = "SubProcess_Confirmation",
            outgoing = listOf("CallActivity_AbortRegistration")),
        FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
            properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
            attachedToRef = "Activity_ConfirmRegistration",
            parentId = "SubProcess_Confirmation",
            outgoing = listOf("Activity_SendConfirmationMail")),
    ),
    sequenceFlows: List<SequenceFlowDefinition> = listOf(
        SequenceFlowDefinition("Flow_05i3x1y", "StartEvent_RequestReceived", "Activity_SendConfirmationMail"),
        SequenceFlowDefinition("Flow_09cuvzp", "SubProcess_Confirmation", "Activity_SendWelcomeMail"),
        SequenceFlowDefinition("Flow_0i2ctuv", "ErrorEvent_InvalidMail", "EndEvent_RegistrationNotPossible"),
        SequenceFlowDefinition("Flow_0x4ewvb", "Timer_EveryDay", "Activity_SendConfirmationMail"),
        SequenceFlowDefinition("Flow_1bckm43", "Activity_SendConfirmationMail", "Activity_ConfirmRegistration"),
        SequenceFlowDefinition("Flow_1bsb8no", "CallActivity_AbortRegistration", "EndEvent_RegistrationAborted"),
        SequenceFlowDefinition("Flow_1cpwe57", "Activity_ConfirmRegistration", "EndEvent_SubscriptionConfirmed"),
        SequenceFlowDefinition("Flow_1csfyyz", "StartEvent_SubmitRegistrationForm", "SubProcess_Confirmation"),
        SequenceFlowDefinition("Flow_1i7hjid", "Activity_SendWelcomeMail", "EndEvent_RegistrationCompleted"),
        SequenceFlowDefinition("Flow_1l1lj4m", "Timer_After3Days", "CallActivity_AbortRegistration"),
    ),
    messages: List<MessageDefinition> = listOf(
        MessageDefinition("StartEvent_SubmitRegistrationForm", "Message_FormSubmitted"),
    ),
    signals: List<SignalDefinition> = listOf(
        SignalDefinition("EndEvent_RegistrationNotPossible", "Signal_RegistrationNotPossible")
    ),
    errors: List<ErrorDefinition> = listOf(
        ErrorDefinition("ErrorEvent_InvalidMail", "Error_InvalidMail", "500")
    ),
    escalations: List<EscalationDefinition> = emptyList(),
    compensations: List<CompensationDefinition> = emptyList(),
) = testBpmnModel(
    processId = processId,
    variantName = variantName,
    flowNodes = flowNodes,
    sequenceFlows = sequenceFlows,
    messages = messages,
    signals = signals,
    errors = errors,
    escalations = escalations,
    compensations = compensations,
)
