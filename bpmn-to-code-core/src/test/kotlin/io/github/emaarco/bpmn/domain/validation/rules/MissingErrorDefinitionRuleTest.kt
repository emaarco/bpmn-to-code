package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingErrorDefinitionRuleTest {

    private val rule = MissingErrorDefinitionRule()

    @Test
    fun `reports error for error with null name`() {
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = "err1", name = null, code = "500"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
    }

    @Test
    fun `reports error for error with null code`() {
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = "err1", name = "MyError", code = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `no violations for error with all fields`() {
        val model = testBpmnModel(
            errors = listOf(ErrorDefinition(id = "err1", name = "MyError", code = "500"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
