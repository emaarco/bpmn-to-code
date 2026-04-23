package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDirection
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testSendNewsletterBpmnModel
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel

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

class KotlinProcessApiBuilderTest {

    private val underTest = KotlinProcessApiBuilder()

    @Test
    fun `buildApiFile generates correct process API file`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            model = testSubscribeNewsletterBpmnModel(
                flowNodes = buildSubscribeNewsletterFlowNodes(
                    confirmationMailImpl = "#{newsletterSendConfirmationMail}",
                    welcomeMailImpl = "\${newsletterSendWelcomeMail}",
                    registrationCompletedImpl = "newsletter.registrationCompleted",
                    extraVariables = listOf(VariableDefinition("testVariable", VariableDirection.INPUT)),
                ),
            )
        )

        // when: we build the process API file
        val result = underTest.buildApiFile(modelApi)

        // then: a single model file is returned at the root package
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.kt")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")

        val expectedFile = File(requireNotNull(javaClass.getResource("/api/NewsletterSubscriptionProcessApiKotlin.txt")).toURI())
        assertThat(result.content).isEqualTo(expectedFile.readText())
        assertKotlinSyntaxValid(result.content)
    }

    @Test
    fun `buildApiFile generates variant-scoped Flows and Relations for merged model`() {

        // given: a merged model with a single variant
        val send = testSendNewsletterBpmnModel(variantName = "send")
        val merged = MergedBpmnModel(
            processId = send.processId,
            flowNodes = send.flowNodes,
            messages = send.messages,
            signals = send.signals,
            errors = send.errors,
            escalations = send.escalations,
            variants = listOf(
                VariantData("send", send.sequenceFlows, send.flowNodes),
            ),
        )
        val modelApi = BpmnModelApi(merged, OutputLanguage.KOTLIN, "de.emaarco.example", ProcessEngine.ZEEBE)

        // when: we build the process API file
        val result = underTest.buildApiFile(modelApi)

        // then: output contains Variants section instead of flat Flows/Relations
        val expectedFile = File(requireNotNull(javaClass.getResource("/api/MultiVariantProcessApiKotlin.txt")).toURI())
        assertThat(result.content).isEqualTo(expectedFile.readText())
        assertKotlinSyntaxValid(result.content)
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
