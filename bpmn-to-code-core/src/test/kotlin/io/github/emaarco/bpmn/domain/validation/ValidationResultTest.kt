package io.github.emaarco.bpmn.domain.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidationResultTest {

    @Test
    fun `empty result is valid with no failures`() {
        val result = ValidationResult(emptyList())
        assertThat(result.isValid).isTrue()
        assertThat(result.hasErrors).isFalse()
        assertThat(result.errors).isEmpty()
        assertThat(result.warnings).isEmpty()
        assertThat(result.hasFailures(failOnWarning = false)).isFalse()
        assertThat(result.hasFailures(failOnWarning = true)).isFalse()
    }

    @Test
    fun `result with errors has failures regardless of failOnWarning`() {
        val result = ValidationResult(
            listOf(violation(Severity.ERROR))
        )
        assertThat(result.isValid).isFalse()
        assertThat(result.hasErrors).isTrue()
        assertThat(result.errors).hasSize(1)
        assertThat(result.warnings).isEmpty()
        assertThat(result.hasFailures(failOnWarning = false)).isTrue()
        assertThat(result.hasFailures(failOnWarning = true)).isTrue()
    }

    @Test
    fun `result with only warnings does not fail by default`() {
        val result = ValidationResult(
            listOf(violation(Severity.WARN))
        )
        assertThat(result.isValid).isFalse()
        assertThat(result.hasErrors).isFalse()
        assertThat(result.errors).isEmpty()
        assertThat(result.warnings).hasSize(1)
        assertThat(result.hasFailures(failOnWarning = false)).isFalse()
    }

    @Test
    fun `result with only warnings fails when failOnWarning is true`() {
        val result = ValidationResult(
            listOf(violation(Severity.WARN))
        )
        assertThat(result.hasFailures(failOnWarning = true)).isTrue()
    }

    @Test
    fun `result with mixed errors and warnings`() {
        val result = ValidationResult(
            listOf(violation(Severity.ERROR), violation(Severity.WARN), violation(Severity.WARN))
        )
        assertThat(result.errors).hasSize(1)
        assertThat(result.warnings).hasSize(2)
        assertThat(result.hasErrors).isTrue()
        assertThat(result.isValid).isFalse()
    }

    private fun violation(severity: Severity) = ValidationViolation(
        ruleId = "test-rule",
        severity = severity,
        elementId = "element1",
        processId = "process1",
        message = "Test violation",
    )
}
