package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingMessageNameRuleTest {

    private val rule = MissingMessageNameRule()

    @Test
    fun `reports error for message with null name`() {
        val model = testBpmnModel(
            messages = listOf(MessageDefinition(id = "msg1", name = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].elementId).isEqualTo("msg1")
    }

    @Test
    fun `no violations for message with valid name`() {
        val model = testBpmnModel(
            messages = listOf(MessageDefinition(id = "msg1", name = "MyMessage"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
