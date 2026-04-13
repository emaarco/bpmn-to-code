package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class JavaSharedTypesBuilderTest {

    private val underTest = JavaSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates all 5 shared type files`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.JAVA)

        // then: exactly 5 files in the types sub-package
        assertThat(results).hasSize(5)
        assertThat(results.map { it.packagePath }).allMatch { it == "de.emaarco.example.types" }
        assertThat(results.map { it.fileName }).containsExactlyInAnyOrder(
            "BpmnTimer.java", "BpmnError.java", "BpmnEscalation.java", "BpmnFlow.java", "BpmnRelations.java"
        )

        // and: each file matches its expected fixture
        results.forEach { typeFile ->
            val fixtureName = typeFile.fileName.replace(".java", "Java.txt")
            val fixtureResource = requireNotNull(javaClass.getResource("/api/types/$fixtureName")) {
                "Missing fixture: /api/types/$fixtureName"
            }
            assertThat(typeFile.content).isEqualToIgnoringWhitespace(File(fixtureResource.toURI()).readText())
        }
    }

}
