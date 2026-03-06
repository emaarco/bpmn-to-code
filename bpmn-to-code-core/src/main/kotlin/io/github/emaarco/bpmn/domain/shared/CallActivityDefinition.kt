package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class CallActivityDefinition(
    private val id: String,
    private val calledElement: String,
) : VariableMapping<String> {
    override fun getName() = id.toUpperSnakeCase()
    override fun getValue() = calledElement
    override fun getRawName() = id
}
