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

@OptIn(K1Deprecation::class)
private val kotlinEnvironment by lazy {
    val config = CompilerConfiguration()
    config.put(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(), config, EnvironmentConfigFiles.JVM_CONFIG_FILES)
}

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

class KotlinSharedTypesBuilderTest {

    private val underTest = KotlinSharedTypesBuilder()

    @Test
    fun `buildTypeFiles generates all 5 shared type files`() {

        // when: we build the shared type files
        val results = underTest.buildTypeFiles("de.emaarco.example", OutputLanguage.KOTLIN)

        // then: exactly 5 files in the types sub-package
        assertThat(results).hasSize(5)
        assertThat(results.map { it.packagePath }).allMatch { it == "de.emaarco.example.types" }
        assertThat(results.map { it.fileName }).containsExactlyInAnyOrder(
            "BpmnTimer.kt", "BpmnError.kt", "BpmnEscalation.kt", "BpmnFlow.kt", "BpmnRelations.kt"
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
    }

}
