package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class SignalDefinition(
    private val id: String,
) : VariableMapping<String> {
    override fun getName() = id.toUpperSnakeCase()
    override fun getValue() = id
}