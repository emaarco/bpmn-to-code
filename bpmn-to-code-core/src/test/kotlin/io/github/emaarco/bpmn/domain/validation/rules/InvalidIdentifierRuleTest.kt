package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InvalidIdentifierRuleTest {

    private val rule = InvalidIdentifierRule()

    @Test
    fun `reports warning for flow node producing invalid identifier`() {
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "123-invalid"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.WARN)
        assertThat(violations[0].message).contains("invalid identifier")
    }

    @Test
    fun `reports warning for timer producing invalid identifier`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "123-timer", properties = FlowNodeProperties.Timer(TimerDefinition(id = "123-timer", type = "Duration", value = "PT1H")))
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        // Both the flow node and the timer are checked, so two violations are expected
        assertThat(violations).hasSize(2)
        assertThat(violations).allMatch { it.severity == Severity.WARN }
        assertThat(violations).allMatch { it.message.contains("invalid identifier") }
        assertThat(violations).anyMatch { it.message.contains("FlowNode") }
        assertThat(violations).anyMatch { it.message.contains("Timer") }
    }

    @Test
    fun `no violations for valid identifiers`() {
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "Activity_SendMail"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `no violations for timer with valid identifier`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "Timer_After3Days", properties = FlowNodeProperties.Timer(TimerDefinition(id = "Timer_After3Days", type = "Duration", value = "PT1H")))
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
