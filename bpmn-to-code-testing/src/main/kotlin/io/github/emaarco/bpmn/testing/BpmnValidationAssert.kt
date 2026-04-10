package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.github.emaarco.bpmn.domain.validation.ValidationViolation
import org.assertj.core.api.AbstractAssert

/**
 * AssertJ assertion wrapper for [ValidationResult].
 *
 * Provides a fluent API for asserting the outcome of BPMN validation.
 */
class BpmnValidationAssert(
    actual: ValidationResult,
) : AbstractAssert<BpmnValidationAssert, ValidationResult>(actual, BpmnValidationAssert::class.java) {

    /**
     * Asserts that the validation produced no violations at all.
     */
    fun assertNoViolations(): BpmnValidationAssert {
        if (actual.violations.isNotEmpty()) {
            failWithMessage(
                "Expected no violations but found %d:\n%s",
                actual.violations.size,
                formatViolations(actual.violations),
            )
        }
        return this
    }

    /**
     * Asserts that the validation produced no violations for the given rule.
     */
    fun assertNoViolations(ruleId: String): BpmnValidationAssert {
        val matching = actual.violations.filter { it.ruleId == ruleId }
        if (matching.isNotEmpty()) {
            failWithMessage(
                "Expected no violations for rule '%s' but found %d:\n%s",
                ruleId,
                matching.size,
                formatViolations(matching),
            )
        }
        return this
    }

    /**
     * Asserts that the validation produced at least one violation.
     */
    fun assertHasViolations(): BpmnValidationAssert {
        if (actual.violations.isEmpty()) {
            failWithMessage("Expected at least one violation but found none")
        }
        return this
    }

    /**
     * Asserts that the validation produced no ERROR-severity violations.
     */
    fun assertNoErrors(): BpmnValidationAssert {
        if (actual.errors.isNotEmpty()) {
            failWithMessage(
                "Expected no errors but found %d:\n%s",
                actual.errors.size,
                formatViolations(actual.errors),
            )
        }
        return this
    }

    /**
     * Asserts that the validation produced no WARN-severity violations.
     */
    fun assertNoWarnings(): BpmnValidationAssert {
        if (actual.warnings.isNotEmpty()) {
            failWithMessage(
                "Expected no warnings but found %d:\n%s",
                actual.warnings.size,
                formatViolations(actual.warnings),
            )
        }
        return this
    }

    /**
     * Returns the underlying [ValidationResult] for custom assertions.
     */
    fun result(): ValidationResult {
        return actual
    }

    companion object {

        /**
         * Entry point for [BpmnValidationAssert].
         */
        @JvmStatic
        fun assertThat(result: ValidationResult): BpmnValidationAssert {
            return BpmnValidationAssert(result)
        }

        private fun formatViolations(violations: List<ValidationViolation>): String {
            return violations.joinToString("\n") { violation ->
                val severity = if (violation.severity == Severity.ERROR) "ERROR" else "WARN"
                val location = if (violation.elementId != null) {
                    "${violation.processId}/${violation.elementId}"
                } else {
                    violation.processId
                }
                "[$severity] $location: ${violation.message} (rule: ${violation.ruleId})"
            }
        }
    }
}
