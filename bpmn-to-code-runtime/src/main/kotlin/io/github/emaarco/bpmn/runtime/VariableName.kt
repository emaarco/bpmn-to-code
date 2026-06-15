package io.github.emaarco.bpmn.runtime

@Deprecated(
    message = "Moved to io.miragon.bpmn.runtime as part of the io.miragon namespace migration. " +
        "Regenerate your Process API and switch the import. This alias will be removed in 4.0.",
    replaceWith = ReplaceWith("VariableName", "io.miragon.bpmn.runtime.VariableName"),
)
sealed interface VariableName {
    val value: String

    data class Input(override val value: String) : VariableName {
        override fun toString(): String = value
    }

    data class Output(override val value: String) : VariableName {
        override fun toString(): String = value
    }

    data class InOut(override val value: String) : VariableName {
        override fun toString(): String = value
    }
}
