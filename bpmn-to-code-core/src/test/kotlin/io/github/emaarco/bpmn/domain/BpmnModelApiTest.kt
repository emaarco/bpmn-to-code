package io.github.emaarco.bpmn.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BpmnModelApiTest {

    @Test
    fun `should return expected file name`() {
        val processIds = listOf("newsletterSubscription", "newsletter-subscription", "newsletter_subscription")
        val expectedFileName = "NewsletterSubscriptionProcessApiV1"
        processIds.forEach { id ->
            val model = testBpmnModel(processId = id)
            val api = testBpmnModelApi(model = model, apiVersion = 1)
            assertThat(api.fileName()).isEqualTo(expectedFileName)
        }
    }

}