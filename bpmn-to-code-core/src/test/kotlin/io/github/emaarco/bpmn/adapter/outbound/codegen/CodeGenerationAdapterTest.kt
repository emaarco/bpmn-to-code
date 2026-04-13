package io.github.emaarco.bpmn.adapter.outbound.codegen

import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CodeGenerationAdapterTest {

    private val kotlinProcessBuilder = mockk<CodeGenerationAdapter.AbstractProcessApiBuilder<*>>(relaxed = true)
    private val kotlinTypesBuilder = mockk<CodeGenerationAdapter.AbstractSharedTypesBuilder>(relaxed = true)
    private val underTest = CodeGenerationAdapter(
        processApiBuilders = mapOf(OutputLanguage.KOTLIN to kotlinProcessBuilder),
        sharedTypesBuilders = mapOf(OutputLanguage.KOTLIN to kotlinTypesBuilder),
    )

    @Test
    fun `creates api-file using builders`() {
        val modelApi = testBpmnModelApi()
        val processFile = GeneratedApiFile(fileName = "TestApi.kt", packagePath = "packagePath", content = "content", language = OutputLanguage.KOTLIN)
        val typeFile = GeneratedApiFile(fileName = "BpmnTimer.kt", packagePath = "packagePath.types", content = "content", language = OutputLanguage.KOTLIN)

        every { kotlinProcessBuilder.buildApiFile(modelApi) } returns processFile
        every { kotlinTypesBuilder.buildTypeFiles("packagePath", OutputLanguage.KOTLIN) } returns listOf(typeFile)

        val result = underTest.generateCode(modelApi)

        verify { kotlinProcessBuilder.buildApiFile(modelApi) }
        verify { kotlinTypesBuilder.buildTypeFiles("packagePath", OutputLanguage.KOTLIN) }
        assert(result == listOf(processFile, typeFile))
        confirmVerified(kotlinProcessBuilder, kotlinTypesBuilder)
    }

    @Test
    fun `throws exception when output language is not supported`() {
        val modelApi = testBpmnModelApi(language = OutputLanguage.JAVA)
        assertThrows(IllegalArgumentException::class.java) {
            underTest.generateCode(modelApi)
        }
    }
}
