package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingProcessIdRuleTest {

    private val underTest = MissingProcessIdRule()

    @Test
    fun `reports error for blank process id`() {

        // given: a model with an empty process ID
        val model = testBpmnModel(processId = "")

        // when / then: an ERROR violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `no violations for valid process id`() {

        // given: a model with a non-blank process ID
        val model = testBpmnModel(processId = "my-process")

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
