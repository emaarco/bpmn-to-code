package io.github.emaarco.bpmn.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BpmnResourceTest {

    private val content = "content".encodeToByteArray()
    private val resource = BpmnResource(fileName = "file.bpmn", content = content)

    @Test
    fun `equals returns true for same instance`() {
        assertThat(resource).isEqualTo(resource)
    }

    @Test
    fun `equals returns true for identical content`() {
        val other = BpmnResource(fileName = "file.bpmn", content = "content".encodeToByteArray())
        assertThat(resource).isEqualTo(other)
    }

    @Test
    fun `equals returns false for different type`() {
        assertThat(resource).isNotEqualTo("not a BpmnResource")
    }

    @Test
    fun `equals returns false for different fileName`() {
        val other = BpmnResource(fileName = "other.bpmn", content = content)
        assertThat(resource).isNotEqualTo(other)
    }

    @Test
    fun `equals returns false for different content`() {
        val other = BpmnResource(fileName = "file.bpmn", content = "other".encodeToByteArray())
        assertThat(resource).isNotEqualTo(other)
    }

    @Test
    fun `hashCode is consistent for identical content`() {
        val other = BpmnResource(fileName = "file.bpmn", content = "content".encodeToByteArray())
        assertThat(resource.hashCode()).isEqualTo(other.hashCode())
    }
}
