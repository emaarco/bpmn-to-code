package io.miragon.bpmn.domain.validation.rules

import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.testBpmnModel
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EngineMismatchRuleTest {

    private val underTest = EngineMismatchRule()

    @Test
    fun `reports an error when the model targets a different engine`() {

        // given: a model detected as Camunda 7 but validated for Operaton (the reported case)
        val model = testBpmnModel(detectedEngine = ProcessEngine.CAMUNDA_7)

        // when / then: a single engine-mismatch ERROR is produced
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.OPERATON))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].ruleId).isEqualTo("engine-mismatch")
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].message).contains("Camunda 7", "Operaton")
    }

    @Test
    fun `no violation when the detected engine matches the selected engine`() {

        // given: a model whose detected engine matches the selected one
        val model = testBpmnModel(detectedEngine = ProcessEngine.ZEEBE)

        // when / then: no violation is reported
        assertThat(underTest.validate(ValidationContext(model = model, engine = ProcessEngine.ZEEBE))).isEmpty()
    }

    @Test
    fun `warns when the source engine could not be detected`() {

        // given: a model whose target engine could not be determined
        val model = testBpmnModel(detectedEngine = null)

        // when / then: a single engine-mismatch WARN is produced
        val violations = underTest.validate(ValidationContext(model = model, engine = ProcessEngine.CAMUNDA_7))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].ruleId).isEqualTo("engine-mismatch")
        assertThat(violations[0].severity).isEqualTo(Severity.WARN)
        assertThat(violations[0].message).contains("Could not determine", "Camunda 7")
    }
}
