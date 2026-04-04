package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingElementIdRuleTest {

    private val rule = MissingElementIdRule()

    @Test
    fun `reports error for flow node with null id`() {
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("FlowNode has no ID")
    }

    @Test
    fun `reports error for service task with null id`() {
        val model = testBpmnModel(
            serviceTasks = listOf(ServiceTaskDefinition(id = null, type = "someType"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("ServiceTask has no ID")
    }

    @Test
    fun `reports error for message with null id`() {
        val model = testBpmnModel(
            messages = listOf(MessageDefinition(id = null, name = "someName"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("Message has no ID")
    }

    @Test
    fun `reports error for signal with null id`() {
        val model = testBpmnModel(
            signals = listOf(SignalDefinition(id = null, name = "someName"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("Signal has no ID")
    }

    @Test
    fun `reports error for error definition with null id`() {
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = null, name = "errName", code = "errCode"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("Error has no ID")
    }

    @Test
    fun `reports error for timer with null id`() {
        val model = testBpmnModel(
            timers = listOf(TimerDefinition(id = null, type = "Duration", value = "PT1H"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("Timer has no ID")
    }

    @Test
    fun `reports error for call activity with null id`() {
        val model = testBpmnModel(
            callActivities = listOf(CallActivityDefinition(id = null, calledElement = "someProcess"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("CallActivity has no ID")
    }

    @Test
    fun `no violations for elements with valid ids`() {
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "Activity_SendMail"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
