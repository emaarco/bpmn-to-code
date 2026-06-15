package io.github.emaarco.bpmn.runtime

@Deprecated(
    message = "Moved to io.miragon.bpmn.runtime as part of the io.miragon namespace migration. " +
        "Regenerate your Process API and switch the import. This alias will be removed in 4.0.",
    replaceWith = ReplaceWith("BpmnRelations", "io.miragon.bpmn.runtime.BpmnRelations"),
)
data class BpmnRelations(
    val name: String? = null,
    val previousElements: List<String>,
    val followingElements: List<String>,
    val parentId: String?,
    val attachedToRef: String?,
    val attachedElements: List<String>,
)
