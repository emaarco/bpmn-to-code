package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BpmnJsonGeneratorTest {

    private val underTest = BpmnJsonGenerator()

    @Test
    fun `generates correct JSON for newsletter model`() {

        // given: the newsletter BPMN model
        val model = testNewsletterBpmnModel()

        // when: generating JSON
        val result = underTest.generate(model)

        // then: expect the generated JSON to match the expected snapshot
        val expectedFile = File(javaClass.getResource("/json/NewsletterSubscriptionProcess.json")!!.toURI())
        val expectedContent = expectedFile.readText()
        assertThat(result).isEqualToIgnoringWhitespace(expectedContent)
    }
}
