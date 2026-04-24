package io.github.emaarco.bpmn.adapter.outbound.codegen

import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CodeGenerationAdapterTest {

    private val kotlinProcessBuilder = mockk<CodeGenerationAdapter.AbstractProcessApiBuilder<*>>(relaxed = true)
    private val underTest = CodeGenerationAdapter(
        processApiBuilders = mapOf(OutputLanguage.KOTLIN to kotlinProcessBuilder),
    )

    @Test
    fun `generateCode delegates to the process api builder and returns its file`() {

        // given: a model API and a stubbed process builder response
        val modelApi = testBpmnModelApi()
        val processFile = GeneratedApiFile(
            fileName = "TestApi.kt",
            packagePath = "packagePath",
            content = "content",
            language = OutputLanguage.KOTLIN,
        )
        every { kotlinProcessBuilder.buildApiFile(modelApi) } returns processFile

        // when: generating code for a Kotlin model
        val result = underTest.generateCode(modelApi)

        // then: the process builder is called and its file returned
        verify { kotlinProcessBuilder.buildApiFile(modelApi) }
        assertThat(result).isEqualTo(listOf(processFile))
        confirmVerified(kotlinProcessBuilder)
    }

    @Test
    fun `generateCode throws when output language is not supported`() {

        // given: a model API with an unsupported language
        val modelApi = testBpmnModelApi(language = OutputLanguage.JAVA)

        // when / then: an exception is thrown
        assertThatThrownBy { underTest.generateCode(modelApi) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
