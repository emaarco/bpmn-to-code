package io.github.emaarco.bpmn

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.ZeebeModelExtractor
import io.github.emaarco.bpmn.application.ProcessApiGeneration
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * JVM half of the cross-target parity gate: the JVM extractor + [ProcessApiGeneration] must produce
 * the committed reference. The JS [ZeebeParityTest] asserts the same, so JVM == reference == JS.
 */
class ZeebeParityReferenceTest {

    @Test
    fun `jvm parse and generate matches the committed parity reference`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c8-subscribe-newsletter.bpmn"))
        val bytes = File(resourceUrl.toURI()).readBytes()
        val reference = File(
            "src/jsTest/resources/api/NewsletterSubscriptionProcessApiKotlin.jvm-reference.txt"
        ).readText()

        val model = ZeebeModelExtractor().extract(bytes)
        val generated = ProcessApiGeneration.generate(
            models = listOf(model),
            config = ProcessApiGeneration.Config(
                packagePath = "de.emaarco.example",
                outputLanguage = OutputLanguage.KOTLIN,
                engine = ProcessEngine.ZEEBE,
            ),
        )

        assertThat(generated).hasSize(1)
        assertThat(generated.single().content).isEqualTo(reference)
    }
}
