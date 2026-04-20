package io.github.emaarco.bpmn.domain.shared

/**
 * Programming languages supported by the process-api generator as output
 */
enum class OutputLanguage(val experimental: Boolean = false) {
    KOTLIN,
    JAVA,
    TYPESCRIPT(experimental = true),
    GO(experimental = true),
}