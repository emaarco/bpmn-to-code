package io.github.emaarco.bpmn.domain

data class BpmnResource(
    val fileName: String,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BpmnResource) return false
        return fileName == other.fileName && content.contentEquals(other.content)
    }

    override fun hashCode(): Int = 31 * fileName.hashCode() + content.contentHashCode()
}
