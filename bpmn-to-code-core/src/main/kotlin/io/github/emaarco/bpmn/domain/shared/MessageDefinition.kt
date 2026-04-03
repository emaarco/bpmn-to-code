package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class MessageDefinition(
    val id: String?,
    private val name: String?,
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = name ?: ""
    override fun getRawName() = id ?: ""
    fun hasName() = name != null
}