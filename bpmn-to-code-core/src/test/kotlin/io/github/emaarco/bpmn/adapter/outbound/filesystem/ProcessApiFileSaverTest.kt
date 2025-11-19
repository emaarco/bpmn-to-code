package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.adapter.logger.NoOpLoggerAdapter
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ProcessApiFileSaverTest {

    private val logger = NoOpLoggerAdapter()
    private val underTest = ProcessApiFileSaver(logger)

    @Test
    fun `saver writes multiple files to correct locations`(@TempDir tempDir: File) {

        // given: multiple generated files with different package paths
        val firstFile = GeneratedApiFile(
            fileName = "OrderProcessApi.kt",
            packagePath = "com.example.order",
            content = "// order process api code",
            language = OutputLanguage.KOTLIN
        )
        val secondFile = GeneratedApiFile(
            fileName = "PaymentProcessApi.kt",
            packagePath = "com.example.payment",
            content = "// payment process api code",
            language = OutputLanguage.KOTLIN
        )
        val thirdFile = GeneratedApiFile(
            fileName = "ShippingProcessApi.kt",
            packagePath = "com.example.order.shipping",
            content = "// shipping process api code",
            language = OutputLanguage.KOTLIN
        )

        // when: writeFiles is called
        underTest.writeFiles(listOf(firstFile, secondFile, thirdFile), tempDir.absolutePath)

        // then: all directories are created and files are written with correct content
        val orderFile = File(tempDir, "com/example/order/OrderProcessApi.kt")
        val paymentFile = File(tempDir, "com/example/payment/PaymentProcessApi.kt")
        val shippingFile = File(tempDir, "com/example/order/shipping/ShippingProcessApi.kt")

        assertThat(orderFile.exists()).isTrue()
        assertThat(paymentFile.exists()).isTrue()
        assertThat(shippingFile.exists()).isTrue()

        assertThat(orderFile.readText()).isEqualTo("// order process api code")
        assertThat(paymentFile.readText()).isEqualTo("// payment process api code")
        assertThat(shippingFile.readText()).isEqualTo("// shipping process api code")
    }
}
