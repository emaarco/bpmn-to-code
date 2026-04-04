package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingTimerDefinitionRuleTest {

    private val rule = MissingTimerDefinitionRule()

    @Test
    fun `reports error for timer with null type`() {
        val model = testBpmnModel(
            timers = listOf(TimerDefinition(id = "timer1", type = null, value = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `no violations for timer with type and value`() {
        val model = testBpmnModel(
            timers = listOf(TimerDefinition(id = "timer1", type = "Duration", value = "PT1H"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
