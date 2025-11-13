package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class ServiceTaskDefinition(
    private val id: String,
    private val type: String,
) : VariableMapping<String> {
    override fun getName() = id.toUpperSnakeCase()
    override fun getValue() = type
}