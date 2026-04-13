package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class CompensationDefinition(
    val id: String?,
    val type: CompensationType,
    val customProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = id ?: ""
    override fun getRawName() = id ?: ""

    companion object {
        const val ACTIVITY_REF_KEY = "activityRef"
        const val WAIT_FOR_COMPLETION_KEY = "waitForCompletion"
    }
}

enum class CompensationType {
    CATCHING,
    THROWING,
}
