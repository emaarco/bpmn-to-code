package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingSignalNameRuleTest {

    private val underTest = MissingSignalNameRule()

    @Test
    fun `reports error for signal with null name`() {

        // given: a signal element with no name
        val model = testBpmnModel(
            signals = listOf(SignalDefinition(id = "sig1", name = null))
        )

        // when / then: an ERROR violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `no violations for signal with name`() {

        // given: a signal element with a valid name
        val model = testBpmnModel(
            signals = listOf(SignalDefinition(id = "sig1", name = "MySignal"))
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
