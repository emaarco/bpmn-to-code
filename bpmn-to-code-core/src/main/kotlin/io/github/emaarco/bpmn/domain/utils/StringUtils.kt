package io.github.emaarco.bpmn.domain.utils

import io.github.emaarco.bpmn.domain.utils.StringUtils.removeExpressionSyntax
import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

object StringUtils {

    /**
     * Converts a string to UPPER_SNAKE_CASE following Kotlin/Java constant naming conventions.
     * Strips expression language syntax (e.g., #{...}, ${...}) and special characters.
     * @return The string converted to UPPER_SNAKE_CASE
     * @sample toUpperSnakeCase will convert timerAfter3Days to TIMER_AFTER_3_DAYS
     * @sample toUpperSnakeCase will convert #{sendMailDelegate} to SEND_MAIL_DELEGATE
     */
    fun String.toUpperSnakeCase(): String {
        return this
            .replace(Regex("[#\${}]"), "") // Strip expression language syntax and special chars
            .replace(Regex("[-.]"), "_")
            .replace(Regex("(?<=[a-zA-Z])(?=[0-9])"), "_")
            .replace(Regex("(?<=[0-9])(?=[a-zA-Z])"), "_")
            .replace(Regex("(?<=[a-z])(?=[A-Z])"), "_")
            .uppercase()
    }

    /**
     * BPMN process variables are often referenced using ${variableName} or #{beanName} syntax.
     * These special characters ($, #, {, }) are invalid in generated code identifiers like class- or property names
     * This function strips the expression wrapper while preserving the core identifier.
     *
     * @return The string with expression language syntax removed
     * @sample removeExpressionSyntax("${authors}") returns "authors"
     * @sample removeExpressionSyntax("#{sendMailDelegate}") returns "sendMailDelegate"
     * @sample removeExpressionSyntax("normalString") returns "normalString"
     */
    fun String.removeExpressionSyntax(): String {
        return this.replace(Regex("[#\${}]"), "")
    }
}


