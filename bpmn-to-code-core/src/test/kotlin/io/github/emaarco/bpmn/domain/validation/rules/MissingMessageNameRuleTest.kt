package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingMessageNameRuleTest {

    private val underTest = MissingMessageNameRule()

    @Test
    fun `reports error for message with null name`() {

        // given: a message element with no name
        val model = testBpmnModel(
            messages = listOf(MessageDefinition(id = "msg1", name = null))
        )

        // when / then: an ERROR violation is reported for the message element
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].elementId).isEqualTo("msg1")
    }

    @Test
    fun `no violations for message with valid name`() {

        // given: a message element with a valid name
        val model = testBpmnModel(
            messages = listOf(MessageDefinition(id = "msg1", name = "MyMessage"))
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
