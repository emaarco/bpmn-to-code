package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.VariableDirection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class VariableNameSubtypeTest {

    @Test
    fun `chooseFor picks INPUT when only input direction is present`() {
        val subtype = VariableNameSubtype.chooseFor(setOf(VariableDirection.INPUT))
        assertThat(subtype).isEqualTo(VariableNameSubtype.INPUT)
        assertThat(subtype.simpleName).isEqualTo("Input")
    }

    @Test
    fun `chooseFor picks OUTPUT when only output direction is present`() {
        val subtype = VariableNameSubtype.chooseFor(setOf(VariableDirection.OUTPUT))
        assertThat(subtype).isEqualTo(VariableNameSubtype.OUTPUT)
        assertThat(subtype.simpleName).isEqualTo("Output")
    }

    @Test
    fun `chooseFor picks IN_OUT when both directions are present`() {
        val subtype = VariableNameSubtype.chooseFor(
            setOf(
                VariableDirection.INPUT,
                VariableDirection.OUTPUT,
            )
        )
        assertThat(subtype).isEqualTo(VariableNameSubtype.IN_OUT)
        assertThat(subtype.simpleName).isEqualTo("InOut")
    }

    @Test
    fun `chooseFor rejects empty direction set`() {
        assertThatThrownBy { VariableNameSubtype.chooseFor(emptySet()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("empty direction set")
    }
}
