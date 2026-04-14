package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingServiceTaskImplementationRuleTest {

    private val rule = MissingServiceTaskImplementationRule()

    @Test
    fun `reports error for service task with null type`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "task1", properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")))
            )
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
            flowNodes = listOf(
                FlowNodeDefinition(id = "task1", properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "myWorker"))))
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.CAMUNDA_7))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `engine-specific hint for Camunda 7`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "task1", properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")))
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.CAMUNDA_7))
        assertThat(violations[0].message).contains("camunda:topic")
    }

    @Test
    fun `engine-specific hint for Operaton`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "task1", properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")))
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.OPERATON))
        assertThat(violations[0].message).contains("operaton:topic")
    }
}
