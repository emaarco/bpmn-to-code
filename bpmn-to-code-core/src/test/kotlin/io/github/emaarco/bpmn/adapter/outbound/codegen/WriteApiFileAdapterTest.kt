package io.github.emaarco.bpmn.adapter.outbound.codegen

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class WriteApiFileAdapterTest {

    private val kotlinBuilder = mockk<WriteApiFileAdapter.AbstractApiBuilder<*>>(relaxed = true)
    private val underTest = WriteApiFileAdapter(
        builder = mapOf(OutputLanguage.KOTLIN to kotlinBuilder)
    )

    @Test
    fun `creates api-file using builder`() {
        val modelApi = testBpmnModelApi()
        every { kotlinBuilder.buildApiFile(modelApi) } just Runs
        underTest.writeApiFile(modelApi)
        verify { kotlinBuilder.buildApiFile(modelApi) }
        confirmVerified(kotlinBuilder)
    }

    @Test
    fun `throws exception when output language is not supported`() {
        val modelApi = testBpmnModelApi(language = OutputLanguage.JAVA)
        assertThrows(IllegalArgumentException::class.java) {
            underTest.writeApiFile(modelApi)
        }
    }
}