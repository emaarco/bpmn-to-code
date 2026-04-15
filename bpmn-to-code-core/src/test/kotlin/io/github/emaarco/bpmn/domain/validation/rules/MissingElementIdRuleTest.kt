package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingElementIdRuleTest {

    private val underTest = MissingElementIdRule()

    @Test
    fun `reports error for flow node with null id`() {

        // given: a model containing a flow node without an ID
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = null))
        )

        // when / then: an ERROR violation mentioning "FlowNode has no ID"
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("FlowNode has no ID")
    }

    @Test
    fun `no violations for elements with valid ids`() {

        // given: a flow node with a valid ID
        val model = testBpmnModel(
            flowNodes = listOf(FlowNodeDefinition(id = "Activity_SendMail"))
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
