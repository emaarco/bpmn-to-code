package io.miragon.bpmn.domain.shared

import io.miragon.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class VariableDefinition(
    private val name: String,
    val direction: VariableDirection,
    val valueExpression: String? = null,
) : VariableMapping<String> {
    override fun getName() = name.toUpperSnakeCase()
    override fun getValue() = name
    override fun getRawName() = name
}
