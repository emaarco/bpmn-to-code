package io.github.emaarco.bpmn.domain.validation.rules

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MissingVariantNameRuleTest {

    private val rule = MissingVariantNameRule()

    @Test
    fun `reports error when multiple models share processId without variantName`() {
        val model1 = testBpmnModel(processId = "order-process")
        val model2 = testBpmnModel(processId = "order-process")
        val allModels = listOf(model1, model2)

        val violations = rule.validate(ValidationContext(model1, ProcessEngine.ZEEBE, allModels))
        assertThat(violations).hasSize(1)
        assertThat(violations[0].severity).isEqualTo(Severity.ERROR)
        assertThat(violations[0].processId).isEqualTo("order-process")
    }

    @Test
    fun `no violations when multiple models share processId with variantName`() {
        val model1 = testBpmnModel(processId = "order-process", variantName = "prodDe")
        val model2 = testBpmnModel(processId = "order-process", variantName = "prodAt")
        val allModels = listOf(model1, model2)

        val violations = rule.validate(ValidationContext(model1, ProcessEngine.ZEEBE, allModels))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `no violations for single model without variantName`() {
        val model = testBpmnModel(processId = "order-process")
        val allModels = listOf(model)

        val violations = rule.validate(ValidationContext(model, ProcessEngine.ZEEBE, allModels))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `reports error only from first model to avoid duplicates`() {
        val model1 = testBpmnModel(processId = "order-process")
        val model2 = testBpmnModel(processId = "order-process")
        val allModels = listOf(model1, model2)

        val violationsFromFirst = rule.validate(ValidationContext(model1, ProcessEngine.ZEEBE, allModels))
        val violationsFromSecond = rule.validate(ValidationContext(model2, ProcessEngine.ZEEBE, allModels))

        assertThat(violationsFromFirst).hasSize(1)
        assertThat(violationsFromSecond).isEmpty()
    }

    @Test
    fun `reports error when some models have variantName and some do not`() {
        val model1 = testBpmnModel(processId = "order-process", variantName = "prodDe")
        val model2 = testBpmnModel(processId = "order-process")
        val allModels = listOf(model1, model2)

        val violations = rule.validate(ValidationContext(model1, ProcessEngine.ZEEBE, allModels))
        assertThat(violations).hasSize(1)
    }
}
