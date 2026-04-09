package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.github.emaarco.bpmn.domain.validation.ValidationViolation
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class BpmnValidationAssertTest {

    private val error = ValidationViolation(
        ruleId = "test-rule",
        severity = Severity.ERROR,
        elementId = "Task_1",
        processId = "myProcess",
        message = "Something is wrong",
    )

    private val warning = ValidationViolation(
        ruleId = "warn-rule",
        severity = Severity.WARN,
        elementId = null,
        processId = "myProcess",
        message = "Something might be wrong",
    )

    // -- assertNoViolations --

    @Test
    fun `assertNoViolations passes when no violations`() {
        val result = ValidationResult(emptyList())
        assertThatCode {
            BpmnValidationAssert.assertThat(result).assertNoViolations()
        }.doesNotThrowAnyException()
    }

    @Test
    fun `assertNoViolations fails when violations exist`() {
        val result = ValidationResult(listOf(error))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertNoViolations()
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Expected no violations but found 1")
            .hasMessageContaining("[ERROR] myProcess/Task_1: Something is wrong (rule: test-rule)")
    }

    // -- assertNoViolations(ruleId) --

    @Test
    fun `assertNoViolations for rule passes when no matching violations`() {
        val result = ValidationResult(listOf(error))
        assertThatCode {
            BpmnValidationAssert.assertThat(result).assertNoViolations("other-rule")
        }.doesNotThrowAnyException()
    }

    @Test
    fun `assertNoViolations for rule fails when matching violations exist`() {
        val result = ValidationResult(listOf(error))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertNoViolations("test-rule")
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Expected no violations for rule 'test-rule'")
    }

    // -- assertViolationCount --

    @Test
    fun `assertViolationCount passes with correct count`() {
        val result = ValidationResult(listOf(error, warning))
        assertThatCode {
            BpmnValidationAssert.assertThat(result).assertViolationCount(2)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `assertViolationCount fails with wrong count`() {
        val result = ValidationResult(listOf(error))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertViolationCount(5)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Expected 5 violations but found 1")
    }

    // -- assertViolationCount(ruleId, count) --

    @Test
    fun `assertViolationCount for rule passes with correct count`() {
        val result = ValidationResult(listOf(error, warning))
        assertThatCode {
            BpmnValidationAssert.assertThat(result).assertViolationCount("test-rule", 1)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `assertViolationCount for rule fails with wrong count`() {
        val result = ValidationResult(listOf(error))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertViolationCount("test-rule", 3)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Expected 3 violations for rule 'test-rule' but found 1")
    }

    // -- assertNoErrors --

    @Test
    fun `assertNoErrors passes when only warnings`() {
        val result = ValidationResult(listOf(warning))
        assertThatCode {
            BpmnValidationAssert.assertThat(result).assertNoErrors()
        }.doesNotThrowAnyException()
    }

    @Test
    fun `assertNoErrors fails when errors exist`() {
        val result = ValidationResult(listOf(error))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertNoErrors()
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Expected no errors but found 1")
    }

    // -- assertNoWarnings --

    @Test
    fun `assertNoWarnings passes when only errors`() {
        val result = ValidationResult(listOf(error))
        assertThatCode {
            BpmnValidationAssert.assertThat(result).assertNoWarnings()
        }.doesNotThrowAnyException()
    }

    @Test
    fun `assertNoWarnings fails when warnings exist`() {
        val result = ValidationResult(listOf(warning))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertNoWarnings()
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Expected no warnings but found 1")
    }

    // -- result() --

    @Test
    fun `result returns the underlying ValidationResult`() {
        val result = ValidationResult(listOf(error))
        val assert = BpmnValidationAssert.assertThat(result)
        org.assertj.core.api.Assertions.assertThat(assert.result()).isSameAs(result)
    }

    // -- format: element without elementId --

    @Test
    fun `format shows processId only when elementId is null`() {
        val result = ValidationResult(listOf(warning))
        assertThatThrownBy {
            BpmnValidationAssert.assertThat(result).assertNoViolations()
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("[WARN] myProcess: Something might be wrong (rule: warn-rule)")
    }
}
