package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class VariableDefinition(
    private val name: String,
) : VariableMapping<String> {
    override fun getName() = name.toUpperSnakeCase()
    override fun getValue() = name
}
