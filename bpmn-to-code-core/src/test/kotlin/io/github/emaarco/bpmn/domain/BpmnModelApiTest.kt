package io.github.emaarco.bpmn.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BpmnModelApiTest {

    val processIds = listOf("newsletterSubscription", "newsletter-subscription", "newsletter_subscription")

    @Test
    fun `should return expected versioned file name`() {
        val expectedFileName = "NewsletterSubscriptionProcessApiV15"
        processIds.forEach { id ->
            val model = testBpmnModel(processId = id)
            val api = testBpmnModelApi(model = model, apiVersion = 15)
            assertThat(api.fileName()).isEqualTo(expectedFileName)
        }
    }

    @Test
    fun `should return expected unversioned file name`() {
        val expectedFileName = "NewsletterSubscriptionProcessApi"
        processIds.forEach { id ->
            val model = testBpmnModel(processId = id)
            val api = testBpmnModelApi(model = model, apiVersion = null)
            assertThat(api.fileName()).isEqualTo(expectedFileName)
        }
    }

}