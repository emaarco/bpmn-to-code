package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingCalledElementRuleTest {

    private val underTest = MissingCalledElementRule()

    @Test
    fun `reports error for call activity with null calledElement`() {

        // given: a call activity with no calledElement set
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "call1",
                    properties = FlowNodeProperties.CallActivity(CallActivityDefinition(id = "call1", calledElement = null)),
                )
            )
        )

        // when / then: an ERROR violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `no violations for call activity with calledElement`() {

        // given: a call activity with a valid calledElement reference
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "call1",
                    properties = FlowNodeProperties.CallActivity(CallActivityDefinition(id = "call1", calledElement = "my-sub-process")),
                )
            )
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
