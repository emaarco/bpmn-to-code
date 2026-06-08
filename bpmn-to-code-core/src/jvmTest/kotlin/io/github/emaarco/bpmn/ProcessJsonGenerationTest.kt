package io.github.emaarco.bpmn

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.ZeebeModelExtractor
import io.github.emaarco.bpmn.application.ProcessJsonGeneration
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Exercises the shared [ProcessJsonGeneration] core end-to-end on a real fixture (default
 * json generator + validation config), so the JVM JSON service and the JS CLI share one path.
 */
class ProcessJsonGenerationTest {

    @Test
    fun `generates one json descriptor per process via the shared core`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c8-subscribe-newsletter.bpmn"))
        val model = ZeebeModelExtractor().extract(File(resourceUrl.toURI()).readBytes())

        val files = ProcessJsonGeneration.generate(listOf(model), ProcessEngine.ZEEBE)

        assertThat(files).hasSize(1)
        assertThat(files.single().fileName).endsWith(".json")
        assertThat(files.single().content).contains("\"processId\"")
    }
}
