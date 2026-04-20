package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class TypeScriptSharedTypesBuilderTest {

    private val underTest = TypeScriptSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates ProcessApi type file`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.TYPESCRIPT)

        // then: a single ProcessApi.ts in the types sub-package
        assertThat(results).hasSize(1)
        assertThat(results[0].packagePath).isEqualTo("de.emaarco.example.types")
        assertThat(results[0].fileName).isEqualTo("ProcessApi.ts")

        val expectedFile = File(requireNotNull(javaClass.getResource("/api/types/ProcessApiTypeScript.txt")).toURI())
        assertThat(results[0].content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }
}
