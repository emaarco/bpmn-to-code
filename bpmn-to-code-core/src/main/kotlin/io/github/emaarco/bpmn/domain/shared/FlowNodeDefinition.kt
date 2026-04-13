package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class FlowNodeDefinition(
    val id: String?,
    val elementType: BpmnElementType = BpmnElementType.UNKNOWN,
    val properties: FlowNodeProperties = FlowNodeProperties.None,
    val variables: List<VariableDefinition> = emptyList(),
    val attachedToRef: String? = null,
    val attachedElements: List<String> = emptyList(),
    val parentId: String? = null,
    val incoming: List<String> = emptyList(),
    val outgoing: List<String> = emptyList(),
    val customProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = id ?: ""
    override fun getRawName() = id ?: ""

    companion object {
        const val ASYNC_BEFORE_KEY = "asyncBefore"
        const val ASYNC_AFTER_KEY = "asyncAfter"
        const val EXCLUSIVE_KEY = "exclusive"
    }
}