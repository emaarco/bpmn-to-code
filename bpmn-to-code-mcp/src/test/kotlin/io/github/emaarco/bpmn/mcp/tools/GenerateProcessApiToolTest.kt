package io.github.emaarco.bpmn.mcp.tools

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiInMemoryPlugin
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateProcessApiToolTest {

    private val underTest = CreateProcessApiInMemoryPlugin()

    @Test
    fun `generates Kotlin API from Zeebe BPMN`() {

        // when: generating a Kotlin API from a Zeebe BPMN file
        val result = generate(
            bpmnFile = "c8-subscribe-newsletter.bpmn",
            processName = "newsletter-subscription",
        )

        // then: a Kotlin process API file is produced in the correct package
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
        assertThat(result.any { it.fileName.endsWith(".kt") }).isTrue()
        assertThat(result.all { it.content.contains("package com.example.process") }).isTrue()
    }

    @Test
    fun `generates Java API from Zeebe BPMN`() {

        // when: generating a Java API from a Zeebe BPMN file
        val result = generate(
            bpmnFile = "c8-subscribe-newsletter.bpmn",
            processName = "newsletter-subscription",
            outputLanguage = OutputLanguage.JAVA,
        )

        // then: a Java process API file is produced
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
        assertThat(result.any { it.fileName.endsWith(".java") }).isTrue()
    }

    @Test
    fun `generates API for Camunda 7 engine`() {

        // when: generating for a Camunda 7 BPMN file
        val result = generate(
            bpmnFile = "c7-subscribe-newsletter.bpmn",
            processName = "newsletter-subscription",
            processEngine = ProcessEngine.CAMUNDA_7,
        )

        // then: the process API file is produced
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
    }

    @Test
    fun `generates API for Operaton engine`() {

        // when: generating for an Operaton BPMN file
        val result = generate(
            bpmnFile = "operaton-subscribe-newsletter.bpmn",
            processName = "newsletter-subscription",
            processEngine = ProcessEngine.OPERATON,
        )

        // then: the process API file is produced
        assertThat(result).isNotEmpty()
        assertThat(result.any { it.fileName.contains("NewsletterSubscriptionProcessApi") }).isTrue()
    }

    @Test
    fun `uses custom packagePath`() {

        // when: generating with a custom package path
        val result = generate(
            bpmnFile = "c8-subscribe-newsletter.bpmn",
            processName = "newsletter-subscription",
            packagePath = "com.myapp.workflows",
        )

        // then: all files are in the custom package
        assertThat(result).isNotEmpty()
        assertThat(result.all { it.content.contains("package com.myapp.workflows") }).isTrue()
    }

    @Test
    fun `formats output with filename headers`() {

        // when: generating and formatting with headers
        val result = generate(
            bpmnFile = "c8-subscribe-newsletter.bpmn",
            processName = "newsletter-subscription",
        )
        val formatted = result.joinToString("\n\n") { "// === ${it.fileName} ===\n${it.content}" }

        // then: formatted output contains file headers and the process API class
        assertThat(formatted).contains("// === ")
        assertThat(formatted).contains("NewsletterSubscriptionProcessApi")
    }

    private fun generate(
        bpmnFile: String,
        processName: String,
        outputLanguage: OutputLanguage = OutputLanguage.KOTLIN,
        processEngine: ProcessEngine = ProcessEngine.ZEEBE,
        packagePath: String = "com.example.process",
    ) = underTest.execute(
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
