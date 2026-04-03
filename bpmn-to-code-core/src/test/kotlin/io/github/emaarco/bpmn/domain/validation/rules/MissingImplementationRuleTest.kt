package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingImplementationRuleTest {

    private val rule = MissingImplementationRule()

    @Test
    fun `reports error for service task with null type`() {
        val model = testBpmnModel(
            serviceTasks = listOf(ServiceTaskDefinition(id = "task1", type = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].elementId).isEqualTo("task1")
        assertThat(violations[0].message).contains("zeebe:taskDefinition")
    }

    @Test
    fun `no violations for service task with valid type`() {
        val model = testBpmnModel(
            serviceTasks = listOf(ServiceTaskDefinition(id = "task1", type = "myWorker"))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.CAMUNDA_7))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `engine-specific hint for Camunda 7`() {
        val model = testBpmnModel(
            serviceTasks = listOf(ServiceTaskDefinition(id = "task1", type = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.CAMUNDA_7))
        assertThat(violations[0].message).contains("camunda:topic")
    }

    @Test
    fun `engine-specific hint for Operaton`() {
        val model = testBpmnModel(
            serviceTasks = listOf(ServiceTaskDefinition(id = "task1", type = null))
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.OPERATON))
        assertThat(violations[0].message).contains("operaton:topic")
    }
}
