package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class FlowNodeDefinition(
    val id: String?,
    val elementType: BpmnElementType = BpmnElementType.UNKNOWN,
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = id ?: ""
    override fun getRawName() = id ?: ""
}