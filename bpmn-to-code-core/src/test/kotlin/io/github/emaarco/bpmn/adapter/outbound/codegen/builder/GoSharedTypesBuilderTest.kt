package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class GoSharedTypesBuilderTest {

    private val underTest = GoSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates a single bpmn_types go file`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.GO)

        // then: exactly 1 file in the types sub-package
        assertThat(results).hasSize(1)
        assertThat(results.map { it.packagePath }).allMatch { it == "de.emaarco.example.types" }
        assertThat(results.map { it.fileName }).containsExactly("bpmn_types.go")

        // and: the file matches its expected fixture
        val typeFile = results.first()
        val fixtureResource = requireNotNull(javaClass.getResource("/api/types/BpmnTypesGo.txt")) {
            "Missing fixture: /api/types/BpmnTypesGo.txt"
        }
        assertThat(typeFile.content).isEqualToIgnoringWhitespace(File(fixtureResource.toURI()).readText())
    }
}
