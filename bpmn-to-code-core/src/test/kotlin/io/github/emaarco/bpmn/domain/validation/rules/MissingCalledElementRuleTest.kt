package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingCalledElementRuleTest {

    private val rule = MissingCalledElementRule()

    @Test
    fun `reports error for call activity with null calledElement`() {
        val model = testBpmnModel(
            callActivities = listOf(CallActivityDefinition(id = "call1", calledElement = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `no violations for call activity with calledElement`() {
        val model = testBpmnModel(
            callActivities = listOf(CallActivityDefinition(id = "call1", calledElement = "my-sub-process"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
