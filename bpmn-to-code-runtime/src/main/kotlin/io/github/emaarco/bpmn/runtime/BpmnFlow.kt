package io.github.emaarco.bpmn.runtime

@Deprecated(
    message = "Moved to io.miragon.bpmn.runtime as part of the io.miragon namespace migration. " +
        "Regenerate your Process API and switch the import. This alias will be removed in 4.0.",
    replaceWith = ReplaceWith("BpmnFlow", "io.miragon.bpmn.runtime.BpmnFlow"),
)
data class BpmnFlow(
    val id: String,
    val name: String? = null,
    val sourceRef: String,
    val targetRef: String,
    val condition: String? = null,
    val isDefault: Boolean = false,
)
