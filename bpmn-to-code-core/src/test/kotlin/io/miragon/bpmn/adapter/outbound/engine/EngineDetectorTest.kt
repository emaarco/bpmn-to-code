package io.miragon.bpmn.adapter.outbound.engine

import io.miragon.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EngineDetectorTest {

    @Test
    fun `detects Zeebe from its namespace`() {
        val content = """<definitions xmlns:zeebe="http://camunda.org/schema/zeebe/1.0" />"""
        assertThat(EngineDetector.detect(content)).isEqualTo(ProcessEngine.ZEEBE)
    }

    @Test
    fun `detects Operaton from its namespace`() {
        val content = """<definitions xmlns:operaton="http://operaton.org/schema/1.0/bpmn" />"""
        assertThat(EngineDetector.detect(content)).isEqualTo(ProcessEngine.OPERATON)
    }

    @Test
    fun `detects Camunda 7 from its namespace`() {
        val content = """<definitions xmlns:camunda="http://camunda.org/schema/1.0/bpmn" />"""
        assertThat(EngineDetector.detect(content)).isEqualTo(ProcessEngine.CAMUNDA_7)
    }

    @Test
    fun `prefers Zeebe over the Camunda modeler namespace`() {
        // given: a Zeebe model that also carries the Camunda modeler namespace
        val content = """
            <definitions
                xmlns:modeler="http://camunda.org/schema/modeler/1.0"
                xmlns:zeebe="http://camunda.org/schema/zeebe/1.0" />
        """.trimIndent()

        // then: the modeler namespace must not be mistaken for Camunda 7
        assertThat(EngineDetector.detect(content)).isEqualTo(ProcessEngine.ZEEBE)
    }

    @Test
    fun `returns null for plain BPMN without engine extensions`() {
        val content = """<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" />"""
        assertThat(EngineDetector.detect(content)).isNull()
    }
}
