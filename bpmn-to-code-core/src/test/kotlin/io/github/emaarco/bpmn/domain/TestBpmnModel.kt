package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import java.io.File

fun testBpmnModel(
    processId: String = "order",
    flowNodes: List<FlowNodeDefinition> = listOf(FlowNodeDefinition(id = "create-order")),
    serviceTasks: List<ServiceTaskDefinition> = listOf(ServiceTaskDefinition(id = "taskId", type = "taskType")),
    messages: List<MessageDefinition> = listOf(MessageDefinition(id = "messageId", name = "messageName")),
    signals: List<SignalDefinition> = listOf(SignalDefinition(id = "signalId")),
    errors: List<ErrorDefinition> = listOf(ErrorDefinition(id = "errorId", name = "errorName", code = "errorCode")),
    timers: List<TimerDefinition> = listOf(TimerDefinition(id = "timerId", type = "timerType", value = "PT1H")),
    variables: List<VariableDefinition> = listOf(VariableDefinition("subscriptionId"))
) = BpmnModel(
    processId = processId,
    flowNodes = flowNodes,
    serviceTasks = serviceTasks,
    messages = messages,
    signals = signals,
    errors = errors,
    timers = timers,
    variables = variables
)

fun testBpmnModelApi(
    model: BpmnModel = testBpmnModel(),
    apiVersion: Int? = 1,
    outputFolder: File = File("outputFolder"),
    packagePath: String = "packagePath",
    language: OutputLanguage = OutputLanguage.KOTLIN,
) = BpmnModelApi(
    model = model,
    apiVersion = apiVersion,
    outputFolder = outputFolder,
    packagePath = packagePath,
    outputLanguage = language,
)

fun testNewsletterBpmnModel(
    processId: String = "newsletterSubscription",
    testVariableForTimer: String = "$" + "{testVariable}",
    flowNodes: List<FlowNodeDefinition> = listOf(
        FlowNodeDefinition("Timer_EveryDay"),
        FlowNodeDefinition("Timer_After3Days"),
        FlowNodeDefinition("ErrorEvent_InvalidMail"),
        FlowNodeDefinition("Activity_ConfirmRegistration"),
        FlowNodeDefinition("SubProcess_Confirmation"),
        FlowNodeDefinition("EndEvent_RegistrationAborted"),
        FlowNodeDefinition("EndEvent_SubscriptionConfirmed"),
        FlowNodeDefinition("EndEvent_RegistrationCompleted"),
        FlowNodeDefinition("EndEvent_RegistrationNotPossible"),
        FlowNodeDefinition("Activity_AbortRegistration"),
        FlowNodeDefinition("Activity_SendWelcomeMail"),
        FlowNodeDefinition("Activity_SendConfirmationMail"),
        FlowNodeDefinition("StartEvent_SubmitRegistrationForm"),
        FlowNodeDefinition("StartEvent_RequestReceived")
    ),
    serviceTasks: List<ServiceTaskDefinition> = listOf(
        ServiceTaskDefinition("EndEvent_RegistrationCompleted", "newsletter.registrationCompleted"),
        ServiceTaskDefinition("Activity_AbortRegistration", "newsletter.abortRegistration"),
        ServiceTaskDefinition("Activity_SendWelcomeMail", "newsletter.sendWelcomeMail"),
        ServiceTaskDefinition("Activity_SendConfirmationMail", "newsletter.sendConfirmationMail")
    ),
    messages: List<MessageDefinition> = listOf(
        MessageDefinition("Message_FormSubmitted", "Message_FormSubmitted"),
        MessageDefinition("Message_SubscriptionConfirmed", "Message_SubscriptionConfirmed")
    ),
    signals: List<SignalDefinition> = listOf(
        SignalDefinition("Signal_RegistrationNotPossible")
    ),
    errors: List<ErrorDefinition> = listOf(
        ErrorDefinition("Error_InvalidMail", "Error_InvalidMail", "500")
    ),
    timers: List<TimerDefinition> = listOf(
        TimerDefinition("Timer_EveryDay", "Duration", "PT1M"),
        TimerDefinition("Timer_After3Days", "Duration", testVariableForTimer)
    ),
    variables: List<VariableDefinition> = listOf(VariableDefinition("subscriptionId"))
) = testBpmnModel(
    processId = processId,
    flowNodes = flowNodes,
    serviceTasks = serviceTasks,
    messages = messages,
    signals = signals,
    errors = errors,
    timers = timers,
    variables = variables
)
