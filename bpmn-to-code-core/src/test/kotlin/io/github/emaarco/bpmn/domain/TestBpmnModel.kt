package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import java.io.File

fun testBpmnModel(
    processId: String = "order",
    flowNodes: List<FlowNodeDefinition> = listOf(FlowNodeDefinition(id = "create-order")),
    serviceTasks: List<ServiceTaskDefinition> = listOf(ServiceTaskDefinition(id = "taskId", type = "taskType")),
    messages: List<MessageDefinition> = listOf(MessageDefinition(id = "messageId", name = "messageName"))
) = BpmnModel(
    processId = processId,
    flowNodes = flowNodes,
    serviceTasks = serviceTasks,
    messages = messages
)

fun testBpmnModelApi(
    model: BpmnModel = testBpmnModel(),
    apiVersion: Int = 1,
    outputFolder: File = File("outputFolder"),
    packagePath: String = "packagePath",
    language: OutputLanguage = OutputLanguage.KOTLIN
) = BpmnModelApi(
    model = model,
    apiVersion = apiVersion,
    outputFolder = outputFolder,
    packagePath = packagePath,
    outputLanguage = language,
)

fun testNewsletterBpmnModel() = testBpmnModel(
    processId = "newsletterSubscription",
    flowNodes = listOf(
        FlowNodeDefinition("Timer_EveryDay"),
        FlowNodeDefinition("Timer_After3Days"),
        FlowNodeDefinition("Activity_ConfirmRegistration"),
        FlowNodeDefinition("SubProcess_Confirmation"),
        FlowNodeDefinition("EndEvent_RegistrationAborted"),
        FlowNodeDefinition("EndEvent_RegistrationCompleted"),
        FlowNodeDefinition("EndEvent_SubscriptionConfirmed"),
        FlowNodeDefinition("Activity_AbortRegistration"),
        FlowNodeDefinition("Activity_SendWelcomeMail"),
        FlowNodeDefinition("Activity_SendConfirmationMail"),
        FlowNodeDefinition("StartEvent_SubmitRegistrationForm"),
        FlowNodeDefinition("StartEvent_RequestReceived")
    ),
    serviceTasks = listOf(
        ServiceTaskDefinition("Activity_AbortRegistration", "newsletter.abortRegistration"),
        ServiceTaskDefinition("Activity_SendWelcomeMail", "newsletter.sendWelcomeMail"),
        ServiceTaskDefinition("Activity_SendConfirmationMail", "newsletter.sendConfirmationMail")
    ),
    messages = listOf(
        MessageDefinition("Message_FormSubmitted", "Message_FormSubmitted"),
        MessageDefinition("Message_SubscriptionConfirmed", "Message_SubscriptionConfirmed")
    )
)
