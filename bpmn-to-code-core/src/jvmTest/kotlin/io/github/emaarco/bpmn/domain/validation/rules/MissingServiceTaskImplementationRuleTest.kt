package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingServiceTaskImplementationRuleTest {

    private val underTest = MissingServiceTaskImplementationRule()

    @Test
    fun `reports error for service task with null type`() {

        // given: a service task with no implementation
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")),
                )
            )
        )

        // when: validating against Zeebe
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))

        // then: an ERROR violation mentioning zeebe:taskDefinition
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].elementId).isEqualTo("task1")
        assertThat(violations[0].message).contains("zeebe:taskDefinition")
    }

    @Test
    fun `no violations for service task with valid type`() {

        // given: a service task with a valid implementation
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(
                        ServiceTaskDefinition(id = "task1", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "myWorker"))
                    ),
                )
            )
        )

        // when / then: no violations (for any engine)
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.CAMUNDA_7))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `engine-specific hint for Camunda 7`() {

        // given: a service task with no implementation validated against Camunda 7
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")),
                )
            )
        )

        // when / then: the violation message mentions camunda:topic
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.CAMUNDA_7))
        assertThat(violations[0].message).contains("camunda:topic")
    }

    @Test
    fun `engine-specific hint for Operaton`() {

        // given: a service task with no implementation validated against Operaton
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "task1")),
                )
            )
        )

        // when / then: the violation message mentions operaton:topic
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.OPERATON))
        assertThat(violations[0].message).contains("operaton:topic")
    }
}
