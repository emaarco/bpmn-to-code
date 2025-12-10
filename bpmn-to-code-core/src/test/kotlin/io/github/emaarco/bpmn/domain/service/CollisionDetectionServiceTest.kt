package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.validation.VariableNameCollisionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollisionDetectionServiceTest {

    private val service = CollisionDetectionService()

    @Test
    fun `detectCollisions should not throw when no collisions exist`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("Activity_Task1"),
                FlowNodeDefinition("Activity_Task2"),
            ),
            serviceTasks = listOf(
                ServiceTaskDefinition("Task1", "newsletter.sendMail"),
                ServiceTaskDefinition("Task2", "newsletter.sendConfirmationMail"),
            ),
            messages = listOf(
                MessageDefinition("Message_FormSubmitted", "Message_FormSubmitted"),
                MessageDefinition("Message_SubscriptionConfirmed", "Message_SubscriptionConfirmed"),
            ),
        )

        assertThatCode { service.detectCollisions(listOf(model)) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `detectCollisions should allow true duplicates with same original ID`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            messages = listOf(
                MessageDefinition("Message_Test", "Message_Test"),
                MessageDefinition("Message_Test", "Message_Test"),
            ),
            flowNodes = listOf(
                FlowNodeDefinition("Activity_SendMail"),
                FlowNodeDefinition("Activity_SendMail"),
            ),
        )

        assertThatCode {
            service.detectCollisions(listOf(model))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `detectCollisions should detect collision with case variation in FlowNodes`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("eventData"),
                FlowNodeDefinition("EventData"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(exception.collisions[0].constantName).isEqualTo("EVENT_DATA")
        assertThat(exception.collisions[0].conflictingIds).containsExactlyInAnyOrder("EventData", "eventData")
        assertThat(exception.collisions[0].processId).isEqualTo("TestProcess")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [FlowNode] EVENT_DATA")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collision with separator variation in FlowNodes`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("endEvent_dataProcessed"),
                FlowNodeDefinition("endEvent-dataProcessed"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(exception.collisions[0].constantName).isEqualTo("END_EVENT_DATA_PROCESSED")
        assertThat(exception.collisions[0].conflictingIds).containsExactlyInAnyOrder(
            "endEvent-dataProcessed",
            "endEvent_dataProcessed",
        )
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [FlowNode] END_EVENT_DATA_PROCESSED")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collision with mixed case and separator variation`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("eventData"),
                FlowNodeDefinition("event-data"),
                FlowNodeDefinition("event_Data"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(exception.collisions[0].constantName).isEqualTo("EVENT_DATA")
        assertThat(exception.collisions[0].conflictingIds).containsExactlyInAnyOrder(
            "event-data",
            "eventData",
            "event_Data",
        )
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [FlowNode] EVENT_DATA")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions in Messages`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            messages = listOf(
                MessageDefinition("message_formSubmitted", "FormSubmitted"),
                MessageDefinition("message-formSubmitted", "FormSubmitted"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("Message")
        assertThat(exception.collisions[0].constantName).isEqualTo("MESSAGE_FORM_SUBMITTED")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [Message] MESSAGE_FORM_SUBMITTED")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions in ServiceTasks`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            serviceTasks = listOf(
                ServiceTaskDefinition("task1", "newsletter.sendMail"),
                ServiceTaskDefinition("task2", "newsletter_sendMail"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("ServiceTask")
        assertThat(exception.collisions[0].constantName).isEqualTo("NEWSLETTER_SEND_MAIL")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [ServiceTask] NEWSLETTER_SEND_MAIL")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions in Signals`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            signals = listOf(
                SignalDefinition("signal.complete"),
                SignalDefinition("signal_complete"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("Signal")
        assertThat(exception.collisions[0].constantName).isEqualTo("SIGNAL_COMPLETE")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [Signal] SIGNAL_COMPLETE")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions in Errors`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            errors = listOf(
                ErrorDefinition("Error_InvalidMail", "InvalidMail", "400"),
                ErrorDefinition("Error-InvalidMail", "InvalidMail", "400"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("Error")
        assertThat(exception.collisions[0].constantName).isEqualTo("ERROR_INVALID_MAIL")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [Error] ERROR_INVALID_MAIL")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions in Timers`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            timers = listOf(
                TimerDefinition("Duration", "Duration", "PT1M"),
                TimerDefinition("duration", "Duration", "PT2M"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("Timer")
        assertThat(exception.collisions[0].constantName).isEqualTo("DURATION")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [Timer] DURATION")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions in Variables`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            variables = listOf(
                VariableDefinition("userId"),
                VariableDefinition("user_id"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].variableType).isEqualTo("Variable")
        assertThat(exception.collisions[0].constantName).isEqualTo("USER_ID")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [Variable] USER_ID")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect multiple collisions across different variable types`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("endEvent_complete"),
                FlowNodeDefinition("endEvent-complete"),
            ),
            messages = listOf(
                MessageDefinition("message_sent", "SentMessage1"),
                MessageDefinition("message-sent", "SentMessage2"),
            ),
            signals = listOf(
                SignalDefinition("signal_ready"),
                SignalDefinition("signal-ready"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(3)
        assertThat(exception.collisions.map { it.variableType }).containsExactlyInAnyOrder(
            "FlowNode",
            "Message",
            "Signal",
        )
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [FlowNode] END_EVENT_COMPLETE")
        assertThat(exception.message).contains("  [Message] MESSAGE_SENT")
        assertThat(exception.message).contains("  [Signal] SIGNAL_READY")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should handle mixed valid and collision cases`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("Activity_Task1"),
                FlowNodeDefinition("Activity_Task2"),
                FlowNodeDefinition("Activity_Task3"),
                FlowNodeDefinition("endEvent_complete"),
                FlowNodeDefinition("endEvent-complete"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model))
        }

        assertThat(exception.collisions).hasSize(1)
        assertThat(exception.collisions[0].constantName).isEqualTo("END_EVENT_COMPLETE")
        assertThat(exception.message).contains("Process: TestProcess")
        assertThat(exception.message).contains("  [FlowNode] END_EVENT_COMPLETE")
        assertThat(exception.message).contains("    Conflicting IDs:")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    @Test
    fun `detectCollisions should detect collisions across multiple models`() {
        val model1 = testBpmnModel(
            processId = "Process1",
            flowNodes = listOf(
                FlowNodeDefinition("endEvent_complete"),
                FlowNodeDefinition("endEvent-complete"),
            ),
        )

        val model2 = testBpmnModel(
            processId = "Process2",
            flowNodes = listOf(
                FlowNodeDefinition("startEvent_begin"),
                FlowNodeDefinition("startEvent-begin"),
            ),
        )

        val exception = assertThrows<VariableNameCollisionException> {
            service.detectCollisions(listOf(model1, model2))
        }

        assertThat(exception.collisions).hasSize(2)
        assertThat(exception.collisions.map { it.processId }).containsExactlyInAnyOrder("Process1", "Process2")
        assertThat(exception.message).contains("Process: Process1")
        assertThat(exception.message).contains("  [FlowNode] END_EVENT_COMPLETE")
        assertThat(exception.message).contains("Process: Process2")
        assertThat(exception.message).contains("  [FlowNode] START_EVENT_BEGIN")
        assertThat(exception.message).contains("Please update your BPMN files to use consistent naming.")
    }

    private fun testBpmnModel(
        processId: String,
        flowNodes: List<FlowNodeDefinition> = emptyList(),
        serviceTasks: List<ServiceTaskDefinition> = emptyList(),
        messages: List<MessageDefinition> = emptyList(),
        signals: List<SignalDefinition> = emptyList(),
        errors: List<ErrorDefinition> = emptyList(),
        timers: List<TimerDefinition> = emptyList(),
        variables: List<VariableDefinition> = emptyList(),
    ) = BpmnModel(
        processId = processId,
        flowNodes = flowNodes,
        serviceTasks = serviceTasks,
        messages = messages,
        signals = signals,
        errors = errors,
        timers = timers,
        variables = variables,
    )
}
