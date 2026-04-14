package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingErrorDefinitionRuleTest {

    private val underTest = MissingErrorDefinitionRule()

    @Test
    fun `reports error for error with null name`() {

        // given: an error element with no name
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = "err1", name = null, code = "500"))
        )

        // when / then: an ERROR violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `reports error for error with null code`() {

        // given: an error element with no code
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = "err1", name = "MyError", code = null))
        )

        // when / then: a violation is reported
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `no violations for error with all fields`() {

        // given: a fully defined error element
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = "err1", name = "MyError", code = "500"))
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
