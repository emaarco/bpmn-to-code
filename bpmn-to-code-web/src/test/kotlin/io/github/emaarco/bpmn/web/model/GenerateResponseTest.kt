package io.github.emaarco.bpmn.web.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateResponseTest {

    @Test
    fun `noFilesProvided should return proper error response`() {
        val response = GenerateResponse.noFilesProvided()

        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error).isEqualTo("No files provided")
    }

    @Test
    fun `tooManyFiles should return proper error response`() {
        val response = GenerateResponse.tooManyFiles()

        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error).isEqualTo("Maximum 3 BPMN files allowed")
    }
}
