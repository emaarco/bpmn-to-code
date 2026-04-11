package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_KIND_KEY
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class OperatonModelExtractorTest {

    private val underTest = OperatonModelExtractor()

    @Test
    fun `extract returns valid BpmnModel with operaton namespace`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())

        val opServiceTasks = listOf(
            ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail", IMPL_KIND_KEY to "DELEGATE_EXPRESSION")),
            ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail", IMPL_KIND_KEY to "EXTERNAL_TASK")),
            ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted", IMPL_KIND_KEY to "EXTERNAL_TASK")),
        )
        val opServiceTaskById = opServiceTasks.associateBy { it.id }

        assertThat(bpmnModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
            testNewsletterBpmnModel(
                flowNodes = listOf(
                    FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
                        properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
                        variables = listOf(VariableDefinition("subscriptionId"), VariableDefinition("reasonCode"), VariableDefinition("abortResult"))),
                    FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.RECEIVE_TASK,
                        variables = listOf(VariableDefinition("subscriptionId"))),
                    FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["Activity_SendConfirmationMail"]!!),
                        variables = listOf(VariableDefinition("subscriptionId"), VariableDefinition("otherVariable"))),
                    FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["Activity_SendWelcomeMail"]!!),
                        variables = listOf(VariableDefinition("subscriptionId"))),
                    FlowNodeDefinition("EndEvent_RegistrationAborted", BpmnElementType.END_EVENT),
                    FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["EndEvent_RegistrationCompleted"]!!),
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
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "\${testVariable}")),
                        attachedToRef = "SubProcess_Confirmation"),
                    FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
                        attachedToRef = "Activity_ConfirmRegistration"),
                ),
            )
        )
    }

    @Test
    fun `extract returns additionalVariables from operaton properties`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-additional-variables.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variables).containsExactlyInAnyOrder(
            VariableDefinition("orderId"),
            VariableDefinition("customerEmail"),
            VariableDefinition("amount"),
            VariableDefinition("shipmentId"),
        )
    }

    @Test
    fun `extract returns multi-instance variables`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-multi-instance.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variables).containsExactlyInAnyOrder(
            VariableDefinition("authors"),
            VariableDefinition("author"),
            VariableDefinition("subscribers"),
            VariableDefinition("subscriber"),
        )
    }

}
