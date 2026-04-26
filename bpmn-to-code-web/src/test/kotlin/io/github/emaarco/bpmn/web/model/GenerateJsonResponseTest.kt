package io.github.emaarco.bpmn.web.model

import io.github.emaarco.bpmn.domain.validation.BpmnValidationException
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation
import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateJsonResponseTest {

    @Test
    fun `noFilesProvided should return proper error response`() {
        val response = GenerateJsonResponse.noFilesProvided()

        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error).isEqualTo("No files provided")
    }

    @Test
    fun `tooManyFiles should return proper error response`() {
        val response = GenerateJsonResponse.tooManyFiles()

        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error).isEqualTo("Maximum 3 BPMN files allowed")
    }

    @Test
    fun `unknownError should return internal server error response`() {
        val response = GenerateJsonResponse.unknownError()

        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error).isEqualTo("Unknown error occurred")
        assertThat(response.statusCode).isEqualTo(HttpStatusCode.InternalServerError)
    }

    @Test
    fun `fromValidationException should return bad request response with exception message`() {
        val violation = ValidationViolation(
            ruleId = "rule-1",
            severity = Severity.ERROR,
            elementId = "task-1",
            processId = "my-process",
            message = "Service task is missing implementation",
        )
        val exception = BpmnValidationException(listOf(violation))

        val response = GenerateJsonResponse.fromValidationException(exception)

        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error).isEqualTo(exception.message)
        assertThat(response.statusCode).isEqualTo(HttpStatusCode.BadRequest)
    }
}
