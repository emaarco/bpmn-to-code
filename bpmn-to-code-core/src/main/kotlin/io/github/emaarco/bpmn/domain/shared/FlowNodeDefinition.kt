package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class FlowNodeDefinition(
    val id: String?,
    val elementType: BpmnElementType = BpmnElementType.UNKNOWN,
    val properties: FlowNodeProperties = FlowNodeProperties.None,
    val variables: List<VariableDefinition> = emptyList(),
    val attachedToRef: String? = null,
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = id ?: ""
    override fun getRawName() = id ?: ""
}