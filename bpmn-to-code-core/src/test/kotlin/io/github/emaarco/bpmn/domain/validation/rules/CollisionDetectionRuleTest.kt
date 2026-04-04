package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollisionDetectionRuleTest {

    private val rule = CollisionDetectionRule()

    @Test
    fun `phase is POST_MERGE`() {
        assertThat(rule.phase).isEqualTo(ValidationPhase.POST_MERGE)
    }

    @Test
    fun `reports collision when different IDs normalize to same constant`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "endEvent_complete"),
                FlowNodeDefinition(id = "endEvent-complete"),
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("conflicting IDs")
    }

    @Test
    fun `no violations when no collisions`() {
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "Activity_One"),
                FlowNodeDefinition(id = "Activity_Two"),
            )
        )
        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
