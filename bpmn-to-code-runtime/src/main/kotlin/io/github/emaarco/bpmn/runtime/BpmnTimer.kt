package io.github.emaarco.bpmn.runtime

@Deprecated(
    message = "Moved to io.miragon.bpmn.runtime as part of the io.miragon namespace migration. " +
        "Regenerate your Process API and switch the import. This alias will be removed in 4.0.",
    replaceWith = ReplaceWith("BpmnTimer", "io.miragon.bpmn.runtime.BpmnTimer"),
)
data class BpmnTimer(
    val type: String,
    val timerValue: String,
)
