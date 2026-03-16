package io.github.emaarco.bpmn.mcp.tools

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateProcessApiToolTest {

    private val plugin = CreateProcessApiInMemoryPlugin()

    @Test
    fun `generates Kotlin API from Zeebe BPMN`() {
        val result = generate(
            bpmnFile = "c8-newsletter.bpmn",
            processName = "newsletter-subscription",
        )
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
        assertThat(result.any { it.fileName.endsWith(".kt") }).isTrue()
        assertThat(result.all { it.content.contains("package com.example.process") }).isTrue()
    }

    @Test
    fun `generates Java API from Zeebe BPMN`() {
        val result = generate(
            bpmnFile = "c8-newsletter.bpmn",
            processName = "newsletter-subscription",
            outputLanguage = OutputLanguage.JAVA,
        )
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
        assertThat(result.any { it.fileName.endsWith(".java") }).isTrue()
    }

    @Test
    fun `generates API for Camunda 7 engine`() {
        val result = generate(
            bpmnFile = "c7-newsletter.bpmn",
            processName = "newsletter-subscription",
            processEngine = ProcessEngine.CAMUNDA_7,
        )
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
    }

    @Test
    fun `generates API for Operaton engine`() {
        val result = generate(
            bpmnFile = "operaton-newsletter.bpmn",
            processName = "newsletter-subscription",
            processEngine = ProcessEngine.OPERATON,
        )
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
    }

    @Test
    fun `uses custom packagePath`() {
        val result = generate(
            bpmnFile = "c8-newsletter.bpmn",
            processName = "newsletter-subscription",
            packagePath = "com.myapp.workflows",
        )
        assertThat(result).isNotEmpty()
        assertThat(result.all { it.content.contains("package com.myapp.workflows") }).isTrue()
    }

    @Test
    fun `formats output with filename headers`() {
        val result = generate(
            bpmnFile = "c8-newsletter.bpmn",
            processName = "newsletter-subscription",
        )
        val formatted = result.joinToString("\n\n") { "// === ${it.fileName} ===\n${it.content}" }
        assertThat(formatted).contains("// === ")
        assertThat(formatted).contains("NewsletterSubscriptionProcessApi")
    }

    private fun generate(
        bpmnFile: String,
        processName: String,
        outputLanguage: OutputLanguage = OutputLanguage.KOTLIN,
        processEngine: ProcessEngine = ProcessEngine.ZEEBE,
        packagePath: String = "com.example.process",
    ) = plugin.execute(
        bpmnContents = listOf(
            CreateProcessApiInMemoryPlugin.BpmnInput(
                bpmnXml = loadBpmn(bpmnFile),
                processName = processName,
            )
        ),
        packagePath = packagePath,
        outputLanguage = outputLanguage,
        engine = processEngine,
    )

    private fun loadBpmn(fileName: String): String {
        return javaClass.classLoader.getResource("bpmn/$fileName")?.readText()
            ?: error("Test BPMN file not found: $fileName")
    }
}
