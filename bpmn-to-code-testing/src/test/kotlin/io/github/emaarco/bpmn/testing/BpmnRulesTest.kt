package io.github.emaarco.bpmn.testing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BpmnRulesTest {

    @Test
    fun `all() returns all 11 built-in rules`() {
        val rules = BpmnRules.all()
        assertThat(rules).hasSize(11)
    }

    @Test
    fun `all rules have unique ids`() {
        val ids = BpmnRules.all().map { it.id }
        assertThat(ids).doesNotHaveDuplicates()
    }

    @Test
    fun `each constant has the expected rule id`() {
        assertThat(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION.id).isEqualTo("missing-service-task-implementation")
        assertThat(BpmnRules.MISSING_MESSAGE_NAME.id).isEqualTo("missing-message-name")
        assertThat(BpmnRules.MISSING_ERROR_DEFINITION.id).isEqualTo("missing-error-definition")
        assertThat(BpmnRules.MISSING_SIGNAL_NAME.id).isEqualTo("missing-signal-name")
        assertThat(BpmnRules.MISSING_TIMER_DEFINITION.id).isEqualTo("missing-timer-definition")
        assertThat(BpmnRules.MISSING_CALLED_ELEMENT.id).isEqualTo("missing-called-element")
        assertThat(BpmnRules.MISSING_ELEMENT_ID.id).isEqualTo("missing-element-id")
        assertThat(BpmnRules.INVALID_IDENTIFIER.id).isEqualTo("invalid-identifier")
        assertThat(BpmnRules.EMPTY_PROCESS.id).isEqualTo("empty-process")
        assertThat(BpmnRules.MISSING_PROCESS_ID.id).isEqualTo("missing-process-id")
        assertThat(BpmnRules.COLLISION_DETECTION.id).isEqualTo("collision-detection")
    }

    @Test
    fun `constants are included in all()`() {
        val all = BpmnRules.all()
        assertThat(all).contains(
            BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION,
            BpmnRules.MISSING_MESSAGE_NAME,
            BpmnRules.MISSING_ERROR_DEFINITION,
            BpmnRules.MISSING_SIGNAL_NAME,
            BpmnRules.MISSING_TIMER_DEFINITION,
            BpmnRules.MISSING_CALLED_ELEMENT,
            BpmnRules.MISSING_ELEMENT_ID,
            BpmnRules.INVALID_IDENTIFIER,
            BpmnRules.EMPTY_PROCESS,
            BpmnRules.MISSING_PROCESS_ID,
            BpmnRules.COLLISION_DETECTION,
        )
    }
}
