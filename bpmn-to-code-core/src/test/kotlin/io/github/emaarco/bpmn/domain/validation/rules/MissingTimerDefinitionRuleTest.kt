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

class MissingTimerDefinitionRuleTest {

    private val underTest = MissingTimerDefinitionRule()

    @Test
    fun `reports error for timer with null type`() {

        // given: a timer flow node with no type or value
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "timer1",
                    properties = FlowNodeProperties.Timer(TimerDefinition(id = "timer1", type = null, value = null)),
                )
            )
        )

        // when / then: an ERROR violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `no violations for timer with type and value`() {

        // given: a timer flow node with a valid type and value
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "timer1",
                    properties = FlowNodeProperties.Timer(TimerDefinition(id = "timer1", type = "Duration", value = "PT1H")),
                )
            )
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
