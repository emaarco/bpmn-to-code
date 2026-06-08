package io.github.emaarco.bpmn.domain.validation.model

data class ValidationConfig(
    val failOnWarning: Boolean = false,
    val disabledRules: Set<String> = emptySet(),
)
