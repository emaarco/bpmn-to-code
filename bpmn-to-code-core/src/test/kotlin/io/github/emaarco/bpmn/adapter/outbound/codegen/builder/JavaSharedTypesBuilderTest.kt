package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.sun.source.util.JavacTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

private fun assertJavaSyntaxValid(fileName: String, source: String) {
    val compiler = requireNotNull(ToolProvider.getSystemJavaCompiler())
    val diagnostics = DiagnosticCollector<JavaFileObject>()
    val fileManager = compiler.getStandardFileManager(diagnostics, null, null)
    val sourceObject = object : SimpleJavaFileObject(
        URI.create("string:///${fileName.replace('.', '/')}"), JavaFileObject.Kind.SOURCE
    ) {
        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = source
    }
    val task = compiler.getTask(null, fileManager, diagnostics, null, null, listOf(sourceObject)) as JavacTask
    task.parse()
    val errors = diagnostics.diagnostics.filter { it.kind == Diagnostic.Kind.ERROR }
    assertThat(errors)
        .withFailMessage { "Java syntax errors in generated output: ${errors.map { it.getMessage(null) }}" }
        .isEmpty()
}

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
            assertJavaSyntaxValid(typeFile.fileName, typeFile.content)
        }
    }

}
