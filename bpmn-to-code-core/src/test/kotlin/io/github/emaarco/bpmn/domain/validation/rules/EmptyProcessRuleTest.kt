package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EmptyProcessRuleTest {

    private val underTest = EmptyProcessRule()

    @Test
    fun `reports warning for process with no elements`() {

        // given: a model with no flow nodes
        val model = testBpmnModel(flowNodes = emptyList())

        // when / then: a WARN violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.WARN)
    }

    @Test
    fun `no violations for process with elements`() {

        // given: a model with at least one flow node
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "Activity_Task1"))
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
