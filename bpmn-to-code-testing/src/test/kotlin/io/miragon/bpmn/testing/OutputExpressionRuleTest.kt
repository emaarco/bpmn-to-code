package io.miragon.bpmn.testing

import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.domain.shared.VariableDirection
import io.miragon.bpmn.domain.validation.BpmnValidationRule
import io.miragon.bpmn.domain.validation.model.Severity
import io.miragon.bpmn.domain.validation.model.ValidationContext
import io.miragon.bpmn.domain.validation.model.ValidationViolation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Demonstrates that a custom [BpmnValidationRule] can inspect the value expression (right-hand side)
 * of a variable mapping via `VariableDefinition.valueExpression` (see issue #348).
 */
class OutputExpressionRuleTest {

    /** Allows ${null}, ${true}, ${false} and ${execution.getVariable('...')} as output expressions. */
    private class OutputExpressionAllowListRule : BpmnValidationRule {
        override val id = "output-expression-allow-list"
        override val severity = Severity.ERROR
        private val allowed = Regex("""\$\{(null|true|false|execution\.getVariable\('[^']+'\))}""")

        override fun validate(context: ValidationContext): List<ValidationViolation> {
            return context.model.flowNodes
                .flatMap { node -> node.variables.map { node to it } }
                .filter { (_, variable) -> variable.direction == VariableDirection.OUTPUT }
                .filter { (_, variable) ->
                    val expression = variable.valueExpression
                    expression != null && !allowed.matches(expression)
                }
                .map { (node, variable) ->
                    ValidationViolation(
                        ruleId = id,
                        severity = severity,
                        elementId = node.id,
                        processId = context.model.processId,
                        message = "Output expression '${variable.valueExpression}' is not allowed.",
                    )
                }
        }
    }

    @Test
    fun `custom rule can inspect output parameter value expressions`() {

        // when: validating a process whose output parameters carry value expressions
        val result = BpmnValidator
            .fromClasspath("bpmn/output-mapping-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(OutputExpressionAllowListRule())
            .validate()
            .result()

        // then: only the disallowed expression is flagged - the allow-listed one passes
        assertThat(result.violations).hasSize(1)
        assertThat(result.violations.single().message).contains("\${someBean.compute()}")
    }
}
