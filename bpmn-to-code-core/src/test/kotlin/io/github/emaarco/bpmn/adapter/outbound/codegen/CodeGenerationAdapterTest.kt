package io.github.emaarco.bpmn.adapter.outbound.codegen

import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CodeGenerationAdapterTest {

    private val kotlinBuilder = mockk<CodeGenerationAdapter.AbstractApiBuilder<*>>(relaxed = true)
    private val underTest = CodeGenerationAdapter(
        builder = mapOf(OutputLanguage.KOTLIN to kotlinBuilder)
    )

    @Test
    fun `creates api-file using builder`() {
        val modelApi = testBpmnModelApi()
        val expectedFile = GeneratedApiFile(
            fileName = "test.kt",
            packagePath = "test",
            content = "content",
            language = OutputLanguage.KOTLIN
        )
        every { kotlinBuilder.buildApiFile(modelApi) } returns expectedFile
        val result = underTest.generateCode(modelApi)
        verify { kotlinBuilder.buildApiFile(modelApi) }
        assert(result == expectedFile)
        confirmVerified(kotlinBuilder)
    }

    @Test
    fun `throws exception when output language is not supported`() {
        val modelApi = testBpmnModelApi(language = OutputLanguage.JAVA)
        assertThrows(IllegalArgumentException::class.java) {
            underTest.generateCode(modelApi)
        }
    }
}