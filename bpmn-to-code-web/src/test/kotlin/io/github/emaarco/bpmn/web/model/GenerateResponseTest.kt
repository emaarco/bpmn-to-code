package io.github.emaarco.bpmn.web.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerateResponseTest {

    @Test
    fun `noFilesProvided should return proper error response`() {
        val response = GenerateResponse.noFilesProvided()

        assertFalse(response.success)
        assertTrue(response.files.isEmpty())
        assertEquals("No files provided", response.error)
    }

    @Test
    fun `tooManyFiles should return proper error response`() {
        val response = GenerateResponse.tooManyFiles()

        assertFalse(response.success)
        assertTrue(response.files.isEmpty())
        assertEquals("Maximum 3 BPMN files allowed", response.error)
    }
}
