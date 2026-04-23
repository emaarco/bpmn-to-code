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

class JavaSharedTypesBuilderTest {

    private val underTest = JavaSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates all shared type files`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.JAVA)

        // then: all expected files are emitted in the types sub-package
        assertThat(results).hasSize(11)
        assertThat(results.map { it.packagePath }).allMatch { it == "de.emaarco.example.types" }
        assertThat(results.map { it.fileName }).containsExactlyInAnyOrder(
            "BpmnEngine.java",
            "BpmnTimer.java",
            "BpmnError.java",
            "BpmnEscalation.java",
            "BpmnFlow.java",
            "BpmnRelations.java",
            "ProcessId.java",
            "ElementId.java",
            "MessageName.java",
            "VariableName.java",
            "SignalName.java",
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

        // and: the sealed VariableName declares exactly the subtypes named by VariableNameSubtype
        val variableNameFile = results.first { it.fileName == "VariableName.java" }
        val declaredSubtypes = Regex("""record (\w+)\(""")
            .findAll(variableNameFile.content)
            .map { it.groupValues[1] }
            .toList()
        assertThat(declaredSubtypes).containsExactlyInAnyOrderElementsOf(
            VariableNameSubtype.entries.map { it.simpleName }
        )
    }

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

}
