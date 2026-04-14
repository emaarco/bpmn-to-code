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

    private val underTest = InvalidIdentifierRule()

    @Test
    fun `reports warning for flow node producing invalid identifier`() {

        // given: a flow node whose ID starts with a digit
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "123-invalid"))
        )

        // when: validating
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))

        // then: a WARN violation mentioning "invalid identifier"
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.WARN)
        assertThat(violations[0].message).contains("invalid identifier")
    }

    @Test
    fun `reports warning for timer producing invalid identifier`() {

        // given: a timer flow node whose ID starts with a digit
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "123-timer",
                    properties = FlowNodeProperties.Timer(TimerDefinition(id = "123-timer", type = "Duration", value = "PT1H")),
                )
            )
        )

        // when: validating
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))

        // then: a WARN violation mentioning "invalid identifier"
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.WARN)
        assertThat(violations[0].message).contains("invalid identifier")
    }

    @Test
    fun `no violations for valid identifiers`() {

        // given: a flow node with a valid ID
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "Activity_SendMail"))
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `no violations for timer with valid identifier`() {

        // given: a timer flow node with a valid ID
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "Timer_After3Days",
                    properties = FlowNodeProperties.Timer(TimerDefinition(id = "Timer_After3Days", type = "Duration", value = "PT1H")),
                )
            )
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
