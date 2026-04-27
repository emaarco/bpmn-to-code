package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationType
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
import io.github.emaarco.bpmn.domain.shared.VariableDirection

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

fun testBpmnModelApi(
    model: ProcessModel = testBpmnModel(),
    packagePath: String = "packagePath",
    language: OutputLanguage = OutputLanguage.KOTLIN,
    engine: ProcessEngine = ProcessEngine.ZEEBE,
) = BpmnModelApi(
    model = model,
    packagePath = packagePath,
    outputLanguage = language,
    engine = engine,
)

fun testSubscribeNewsletterBpmnModel(
    processId: String = "newsletterSubscription",
    variantName: String? = null,
    flowNodes: List<FlowNodeDefinition> = listOf(
        FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
            displayName = "Abort registration",
            properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
            variables = listOf(VariableDefinition("subscriptionId", VariableDirection.INPUT)),
            previousElements = listOf("Timer_After3Days"), followingElements = listOf("CompensationEndEvent_RegistrationAborted")),
        FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.USER_TASK,
            displayName = "Confirm subscription",
            attachedElements = listOf("Timer_EveryDay"),
            parentId = "SubProcess_Confirmation",
            previousElements = listOf("Activity_SendConfirmationMail"), followingElements = listOf("EndEvent_SubscriptionConfirmed")),
        FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
            displayName = "Send confirmation mail",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendConfirmationMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail"))),
            variables = listOf(VariableDefinition("subscriptionId", VariableDirection.INPUT)),
            parentId = "SubProcess_Confirmation",
            previousElements = listOf("StartEvent_RequestReceived", "Timer_EveryDay"), followingElements = listOf("Activity_ConfirmRegistration")),
        FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
            displayName = "Send Welcome-Mail",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendWelcomeMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail"))),
            variables = listOf(VariableDefinition("subscriptionId", VariableDirection.INPUT)),
            previousElements = listOf("SubProcess_Confirmation"), followingElements = listOf("EndEvent_RegistrationCompleted"),
            engineSpecificProperties = mapOf(ASYNC_BEFORE_KEY to true, ASYNC_AFTER_KEY to true, EXCLUSIVE_KEY to false)),
        FlowNodeDefinition("CompensationEndEvent_RegistrationAborted", BpmnElementType.END_EVENT,
            displayName = "Registration aborted",
            previousElements = listOf("CallActivity_AbortRegistration")),
        FlowNodeDefinition("CompensationEvent_OnSubscriptionCounter", BpmnElementType.BOUNDARY_EVENT,
            displayName = "Registration aborted",
            attachedToRef = "serviceTask_incrementSubscriptionCounter"),
        FlowNodeDefinition("CompensationTask_DecrementSubscriptionCounter", BpmnElementType.TASK,
            displayName = "Decrement subscription counter"),
        FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
            displayName = "Registration completed",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("EndEvent_RegistrationCompleted", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted"))),
            variables = listOf(VariableDefinition("subscriptionId", VariableDirection.OUTPUT)),
            previousElements = listOf("Activity_SendWelcomeMail")),
        FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT,
            displayName = "Registration not possible",
            previousElements = listOf("ErrorEvent_InvalidMail")),
        FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT,
            displayName = "Subscription confirmed",
            parentId = "SubProcess_Confirmation",
            previousElements = listOf("Activity_ConfirmRegistration")),
        FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
            displayName = "Invalid Mail",
            attachedToRef = "SubProcess_Confirmation",
            followingElements = listOf("EndEvent_RegistrationNotPossible")),
        FlowNodeDefinition("serviceTask_incrementSubscriptionCounter", BpmnElementType.SERVICE_TASK,
            displayName = "Increment subscription counter",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_incrementSubscriptionCounter", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "counterClass"))),
            attachedElements = listOf("CompensationEvent_OnSubscriptionCounter"),
            previousElements = listOf("StartEvent_SubmitRegistrationForm"), followingElements = listOf("SubProcess_Confirmation")),
        FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
            displayName = "Subscription requested",
            variables = listOf(VariableDefinition("subscriptionId", VariableDirection.OUTPUT)),
            parentId = "SubProcess_Confirmation",
            followingElements = listOf("Activity_SendConfirmationMail")),
        FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
            displayName = "Submit newsletter form",
            variables = listOf(VariableDefinition("subscriptionId", VariableDirection.OUTPUT)),
            followingElements = listOf("serviceTask_incrementSubscriptionCounter")),
        FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS,
            displayName = "Subscription Confirmation",
            attachedElements = listOf("ErrorEvent_InvalidMail", "Timer_After3Days"),
            previousElements = listOf("serviceTask_incrementSubscriptionCounter"), followingElements = listOf("Activity_SendWelcomeMail")),
        FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
            displayName = "After 3 days",
            properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "$" + "{testVariable}")),
            attachedToRef = "SubProcess_Confirmation",
            followingElements = listOf("CallActivity_AbortRegistration")),
        FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
            displayName = "Every day",
            properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
            attachedToRef = "Activity_ConfirmRegistration",
            parentId = "SubProcess_Confirmation",
            followingElements = listOf("Activity_SendConfirmationMail")),
    ),
    sequenceFlows: List<SequenceFlowDefinition> = listOf(
        SequenceFlowDefinition("Flow_05i3x1y", "StartEvent_RequestReceived", "Activity_SendConfirmationMail"),
        SequenceFlowDefinition("Flow_09cuvzp", "SubProcess_Confirmation", "Activity_SendWelcomeMail"),
        SequenceFlowDefinition("Flow_0i2ctuv", "ErrorEvent_InvalidMail", "EndEvent_RegistrationNotPossible"),
        SequenceFlowDefinition("Flow_0x4ewvb", "Timer_EveryDay", "Activity_SendConfirmationMail"),
        SequenceFlowDefinition("Flow_0zdmt0t", "serviceTask_incrementSubscriptionCounter", "SubProcess_Confirmation"),
        SequenceFlowDefinition("Flow_1bckm43", "Activity_SendConfirmationMail", "Activity_ConfirmRegistration"),
        SequenceFlowDefinition("Flow_1bsb8no", "CallActivity_AbortRegistration", "CompensationEndEvent_RegistrationAborted"),
        SequenceFlowDefinition("Flow_1cpwe57", "Activity_ConfirmRegistration", "EndEvent_SubscriptionConfirmed"),
        SequenceFlowDefinition("Flow_1csfyyz", "StartEvent_SubmitRegistrationForm", "serviceTask_incrementSubscriptionCounter"),
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
    compensations: List<CompensationDefinition> = listOf(
        CompensationDefinition("CompensationEndEvent_RegistrationAborted", CompensationType.THROWING),
        CompensationDefinition("CompensationEvent_OnSubscriptionCounter", CompensationType.CATCHING),
    ),
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

fun testSendNewsletterBpmnModel(
    processId: String = "sendNewsletter",
    variantName: String? = null,
    flowNodes: List<FlowNodeDefinition> = listOf(
        // Main flow
        FlowNodeDefinition("startEvent_editionCreated", BpmnElementType.START_EVENT,
            followingElements = listOf("serviceTask_loadSubscribers")),
        FlowNodeDefinition("serviceTask_loadSubscribers", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_loadSubscribers", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.loadSubscribers"))),
            variables = listOf(VariableDefinition("subscribers", VariableDirection.OUTPUT), VariableDefinition("author", VariableDirection.OUTPUT)),
            previousElements = listOf("startEvent_editionCreated"), followingElements = listOf("gateway_hasSubscribers")),
        FlowNodeDefinition("gateway_hasSubscribers", BpmnElementType.EXCLUSIVE_GATEWAY,
            previousElements = listOf("serviceTask_loadSubscribers"), followingElements = listOf("serviceTask_sendToSubscriber", "endEvent_noSubscribers")),
        FlowNodeDefinition("serviceTask_sendToSubscriber", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_sendToSubscriber", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendMailToSubscriber"))),
            previousElements = listOf("gateway_hasSubscribers"), followingElements = listOf("serviceTask_notifyAuthor")),
        FlowNodeDefinition("serviceTask_notifyAuthor", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_notifyAuthor", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.notifyAuthor"))),
            previousElements = listOf("serviceTask_sendToSubscriber"), followingElements = listOf("endEvent_editionSent")),
        FlowNodeDefinition("endEvent_editionSent", BpmnElementType.END_EVENT,
            previousElements = listOf("serviceTask_notifyAuthor")),
        FlowNodeDefinition("endEvent_noSubscribers", BpmnElementType.END_EVENT,
            previousElements = listOf("gateway_hasSubscribers")),
        // Event subprocess: error handling
        FlowNodeDefinition("eventSubProcess_errorHandling", BpmnElementType.SUB_PROCESS),
        FlowNodeDefinition("event_mailRejected", BpmnElementType.START_EVENT,
            parentId = "eventSubProcess_errorHandling",
            followingElements = listOf("serviceTask_analyzeError")),
        FlowNodeDefinition("serviceTask_analyzeError", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_analyzeError", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.analyzeSendError"))),
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("event_mailRejected"), followingElements = listOf("gateway_canSendAgain")),
        FlowNodeDefinition("gateway_canSendAgain", BpmnElementType.EXCLUSIVE_GATEWAY,
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("serviceTask_analyzeError"), followingElements = listOf("serviceTask_sendMailAgain", "escalationEndEvent_nofitySupport")),
        FlowNodeDefinition("serviceTask_sendMailAgain", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("serviceTask_sendMailAgain", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendMailToSubscriber"))),
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("gateway_canSendAgain"), followingElements = listOf("eventGateway_afterSendingAgain")),
        FlowNodeDefinition("eventGateway_afterSendingAgain", BpmnElementType.EVENT_BASED_GATEWAY,
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("serviceTask_sendMailAgain"), followingElements = listOf("timer_noRejectionForOneDay", "event_mailRejectedAgain")),
        FlowNodeDefinition("timer_noRejectionForOneDay", BpmnElementType.INTERMEDIATE_CATCH_EVENT,
            properties = FlowNodeProperties.Timer(TimerDefinition("timer_noRejectionForOneDay", "Duration", "PT1D")),
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("eventGateway_afterSendingAgain"), followingElements = listOf("endEvent_issueResolved")),
        FlowNodeDefinition("escalationEndEvent_nofitySupport", BpmnElementType.END_EVENT,
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("gateway_canSendAgain")),
        FlowNodeDefinition("event_mailRejectedAgain", BpmnElementType.INTERMEDIATE_CATCH_EVENT,
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("eventGateway_afterSendingAgain"), followingElements = listOf("escalationEndEvent_nofitySupportAfterRepeatedError")),
        FlowNodeDefinition("escalationEndEvent_nofitySupportAfterRepeatedError", BpmnElementType.END_EVENT,
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("event_mailRejectedAgain")),
        FlowNodeDefinition("endEvent_issueResolved", BpmnElementType.END_EVENT,
            parentId = "eventSubProcess_errorHandling",
            previousElements = listOf("timer_noRejectionForOneDay")),
    ),
    sequenceFlows: List<SequenceFlowDefinition> = listOf(
        // Main flow
        SequenceFlowDefinition("Flow_0bianz5", "startEvent_editionCreated", "serviceTask_loadSubscribers"),
        SequenceFlowDefinition("Flow_04andb8", "serviceTask_loadSubscribers", "gateway_hasSubscribers"),
        SequenceFlowDefinition("Flow_1jogut0", "gateway_hasSubscribers", "serviceTask_sendToSubscriber", flowName = "Yes", isDefault = true),
        SequenceFlowDefinition("Flow_1gsz7wd", "gateway_hasSubscribers", "endEvent_noSubscribers", flowName = "No", conditionExpression = "\${subscribers.size() > 0}"),
        SequenceFlowDefinition("Flow_1ruayvl", "serviceTask_sendToSubscriber", "serviceTask_notifyAuthor"),
        SequenceFlowDefinition("Flow_0v2v55n", "serviceTask_notifyAuthor", "endEvent_editionSent"),
        // Event subprocess
        SequenceFlowDefinition("Flow_0vtppnk", "event_mailRejected", "serviceTask_analyzeError"),
        SequenceFlowDefinition("Flow_13nmnag", "serviceTask_analyzeError", "gateway_canSendAgain"),
        SequenceFlowDefinition("Flow_1izucof", "gateway_canSendAgain", "serviceTask_sendMailAgain", flowName = "Yes", isDefault = true),
        SequenceFlowDefinition("Flow_18nf2jh", "gateway_canSendAgain", "escalationEndEvent_nofitySupport", flowName = "No", conditionExpression = "\${rejection.reason == \"PERMANENT\"}"),
        SequenceFlowDefinition("Flow_0vym6nu", "serviceTask_sendMailAgain", "eventGateway_afterSendingAgain"),
        SequenceFlowDefinition("Flow_0enjkoe", "eventGateway_afterSendingAgain", "timer_noRejectionForOneDay"),
        SequenceFlowDefinition("Flow_081cykl", "eventGateway_afterSendingAgain", "event_mailRejectedAgain"),
        SequenceFlowDefinition("Flow_0x9thpq", "event_mailRejectedAgain", "escalationEndEvent_nofitySupportAfterRepeatedError"),
        SequenceFlowDefinition("Flow_0338xzf", "timer_noRejectionForOneDay", "endEvent_issueResolved"),
    ),
    messages: List<MessageDefinition> = listOf(
        MessageDefinition("event_mailRejected", "Message_MailRejected"),
        MessageDefinition("event_mailRejectedAgain", "Message_MailRejectedAgain"),
    ),
    signals: List<SignalDefinition> = emptyList(),
    errors: List<ErrorDefinition> = emptyList(),
    escalations: List<EscalationDefinition> = listOf(
        EscalationDefinition("escalationEndEvent_nofitySupport", "escalation_notifySupport", "200"),
    ),
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
