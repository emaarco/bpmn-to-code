package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class TypeScriptSharedTypesBuilderTest {

    private val underTest = TypeScriptSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates all 5 shared type files`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.TYPESCRIPT)

        // then: exactly 5 files in the types sub-package
        assertThat(results).hasSize(5)
        assertThat(results.map { it.packagePath }).allMatch { it == "de.emaarco.example.types" }
        assertThat(results.map { it.fileName }).containsExactlyInAnyOrder(
            "BpmnTimer.ts", "BpmnError.ts", "BpmnEscalation.ts", "BpmnFlow.ts", "BpmnRelations.ts"
        )

        // and: each file matches its expected fixture
        results.forEach { typeFile ->
            val fixtureName = typeFile.fileName.replace(".ts", "TypeScript.txt")
            val fixtureResource = requireNotNull(javaClass.getResource("/api/types/$fixtureName")) {
                "Missing fixture: /api/types/$fixtureName"
            }
            assertThat(typeFile.content).isEqualToIgnoringWhitespace(File(fixtureResource.toURI()).readText())
        }
    }
}
