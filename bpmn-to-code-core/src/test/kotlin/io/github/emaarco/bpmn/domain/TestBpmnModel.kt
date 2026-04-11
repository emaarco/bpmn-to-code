package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition

fun testBpmnModel(
    processId: String = "order",
    flowNodes: List<FlowNodeDefinition> = listOf(FlowNodeDefinition(id = "create-order")),
    callActivities: List<CallActivityDefinition> = listOf(CallActivityDefinition(id = "call-activity", calledElement = "called-process")),
    serviceTasks: List<ServiceTaskDefinition> = listOf(ServiceTaskDefinition(id = "taskId", customProperties = mapOf(IMPL_VALUE_KEY to "taskType"))),
    messages: List<MessageDefinition> = listOf(MessageDefinition(id = "messageId", name = "messageName")),
    signals: List<SignalDefinition> = listOf(SignalDefinition(id = "signalId", name = "signalName")),
    errors: List<ErrorDefinition> = listOf(ErrorDefinition(id = "errorId", name = "errorName", code = "errorCode")),
    timers: List<TimerDefinition> = listOf(TimerDefinition(id = "timerId", type = "timerType", value = "PT1H")),
    variables: List<VariableDefinition> = listOf(VariableDefinition("subscriptionId"))
) = BpmnModel(
    processId = processId,
    flowNodes = flowNodes,
    callActivities = callActivities,
    serviceTasks = serviceTasks,
    messages = messages,
    signals = signals,
    errors = errors,
    timers = timers,
    variables = variables
)

fun testBpmnModelApi(
    model: BpmnModel = testBpmnModel(),
    packagePath: String = "packagePath",
    language: OutputLanguage = OutputLanguage.KOTLIN,
    engine: ProcessEngine = ProcessEngine.ZEEBE,
) = BpmnModelApi(
    model = model,
    packagePath = packagePath,
    outputLanguage = language,
    engine = engine,
)

fun testNewsletterBpmnModel(
    processId: String = "newsletterSubscription",
    testVariableForTimer: String = "$" + "{testVariable}",
    callActivities: List<CallActivityDefinition> = listOf(
        CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")
    ),
    flowNodes: List<FlowNodeDefinition> = listOf(
        FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
            properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
            variables = listOf(VariableDefinition("subscriptionId"))),
        FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.RECEIVE_TASK),
        FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail"))),
            variables = listOf(VariableDefinition("subscriptionId"))),
        FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail"))),
            variables = listOf(VariableDefinition("subscriptionId"))),
        FlowNodeDefinition("EndEvent_RegistrationAborted", BpmnElementType.END_EVENT),
        FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted"))),
            variables = listOf(VariableDefinition("subscriptionId"))),
        FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT),
        FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT),
        FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
            attachedToRef = "SubProcess_Confirmation"),
        FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
            variables = listOf(VariableDefinition("subscriptionId"))),
        FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
            variables = listOf(VariableDefinition("subscriptionId"))),
        FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS),
        FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
            properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "$" + "{testVariable}")),
            attachedToRef = "SubProcess_Confirmation"),
        FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
            properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
            attachedToRef = "Activity_ConfirmRegistration"),
    ),
    serviceTasks: List<ServiceTaskDefinition> = listOf(
        ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail")),
        ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail")),
        ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted"))
    ),
    messages: List<MessageDefinition> = listOf(
        MessageDefinition("StartEvent_SubmitRegistrationForm", "Message_FormSubmitted"),
        MessageDefinition("Activity_ConfirmRegistration", "Message_SubscriptionConfirmed")
    ),
    signals: List<SignalDefinition> = listOf(
        SignalDefinition("EndEvent_RegistrationNotPossible", "Signal_RegistrationNotPossible")
    ),
    errors: List<ErrorDefinition> = listOf(
        ErrorDefinition("ErrorEvent_InvalidMail", "Error_InvalidMail", "500")
    ),
    timers: List<TimerDefinition> = listOf(
        TimerDefinition("Timer_After3Days", "Duration", testVariableForTimer),
        TimerDefinition("Timer_EveryDay", "Duration", "PT1M")
    ),
    variables: List<VariableDefinition> = listOf(VariableDefinition("subscriptionId"))
) = testBpmnModel(
    processId = processId,
    flowNodes = flowNodes,
    callActivities = callActivities,
    serviceTasks = serviceTasks,
    messages = messages,
    signals = signals,
    errors = errors,
    timers = timers,
    variables = variables
)
