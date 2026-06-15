package io.github.emaarco.bpmn.runtime

@Deprecated(
    message = "Moved to io.miragon.bpmn.runtime as part of the io.miragon namespace migration. " +
        "Regenerate your Process API and switch the import. This alias will be removed in 4.0.",
    replaceWith = ReplaceWith("MessageName", "io.miragon.bpmn.runtime.MessageName"),
)
data class MessageName(val value: String) {
    override fun toString(): String = value
}
