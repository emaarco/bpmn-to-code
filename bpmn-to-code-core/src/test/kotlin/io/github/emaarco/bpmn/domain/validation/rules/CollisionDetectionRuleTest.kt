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

    private val underTest = CollisionDetectionRule()

    @Test
    fun `phase is POST_MERGE`() {
        assertThat(underTest.phase).isEqualTo(ValidationPhase.POST_MERGE)
    }

    @Test
    fun `reports collision when different IDs normalize to same constant`() {

        // given: two flow nodes that differ only in separator
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "endEvent_complete"),
                FlowNodeDefinition(id = "endEvent-complete"),
            )
        )

        // when: validating
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))

        // then: one error violation referencing conflicting IDs
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("conflicting IDs")
    }

    @Test
    fun `no violations when no collisions`() {

        // given: two flow nodes with distinct constant names
        val model = testBpmnModel(
            flowNodes = listOf(
                FlowNodeDefinition(id = "Activity_One"),
                FlowNodeDefinition(id = "Activity_Two"),
            )
        )

        // when / then: no violations
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))
        assertThat(violations).isEmpty()
    }
}
