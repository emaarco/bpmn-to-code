package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.K1Deprecation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.junit.jupiter.api.Test
import java.io.File

class KotlinSharedTypesBuilderTest {

    private val underTest = KotlinSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates all shared type files`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.KOTLIN)

        // then: all expected files are emitted in the types sub-package
        assertThat(results).hasSize(11)
        assertThat(results.map { it.packagePath }).allMatch { it == "de.emaarco.example.types" }
        assertThat(results.map { it.fileName }).containsExactlyInAnyOrder(
            "BpmnEngine.kt",
            "BpmnTimer.kt",
            "BpmnError.kt",
            "BpmnEscalation.kt",
            "BpmnFlow.kt",
            "BpmnRelations.kt",
            "ProcessId.kt",
            "ElementId.kt",
            "MessageName.kt",
            "VariableName.kt",
            "SignalName.kt",
        )

        // and: each file matches its expected fixture
        results.forEach { typeFile ->
            val fixtureName = typeFile.fileName.replace(".kt", "Kotlin.txt")
            val fixtureResource = requireNotNull(javaClass.getResource("/api/types/$fixtureName")) {
                "Missing fixture: /api/types/$fixtureName"
            }
            assertThat(typeFile.content).isEqualToIgnoringWhitespace(File(fixtureResource.toURI()).readText())
            assertKotlinSyntaxValid(typeFile.content)
        }

        // and: key KDoc blocks are present in the relevant type files
        val relationsFile = results.first { it.fileName == "BpmnRelations.kt" }
        assertThat(relationsFile.content).contains("not sequence-flow ids")

        val flowFile = results.first { it.fileName == "BpmnFlow.kt" }
        assertThat(flowFile.content).contains("default flow")
    }

    companion object {

        @OptIn(K1Deprecation::class)
        private val kotlinEnvironment by lazy {
            val config = CompilerConfiguration()
            config.put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
            KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(), config, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        }

        @OptIn(K1Deprecation::class)
        private fun assertKotlinSyntaxValid(source: String) {
            val file = KtPsiFactory(kotlinEnvironment.project).createFile(source)
            val errors = mutableListOf<String>()
            file.accept(object : KtTreeVisitorVoid() {
                override fun visitErrorElement(element: PsiErrorElement) {
                    errors.add(element.errorDescription)
                }
            })
            assertThat(errors)
                .withFailMessage { "Kotlin syntax errors in generated output: $errors" }
                .isEmpty()
        }
    }
}
