package io.github.emaarco.bpmn.adapter.outbound.engine

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SecureBpmnParserTest {

    @Test
    fun `rejects BPMN files containing DOCTYPE declarations`() {
        val malicious = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"/>
        """.trimIndent().byteInputStream()

        assertThatThrownBy { SecureBpmnParser.readModelFromStream(malicious) }
            .isInstanceOf(SecurityException::class.java)
            .hasMessageContaining("DOCTYPE")
    }

    @Test
    fun `parses valid BPMN files without DOCTYPE`() {
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream("bpmn/c8-subscribe-newsletter.bpmn"))
        assertThatCode { SecureBpmnParser.readModelFromStream(stream) }
            .doesNotThrowAnyException()
    }
}
