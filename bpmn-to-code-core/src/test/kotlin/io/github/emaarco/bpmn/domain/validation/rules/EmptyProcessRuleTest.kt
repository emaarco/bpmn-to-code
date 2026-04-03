package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EmptyProcessRuleTest {

    private val rule = EmptyProcessRule()

    @Test
    fun `reports warning for process with no service tasks`() {
        val model = testBpmnModel(serviceTasks = emptyList())
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.WARN)
    }

    @Test
    fun `no violations for process with service tasks`() {
        val model = testBpmnModel(
            serviceTasks = listOf(ServiceTaskDefinition(id = "task1", type = "worker"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
