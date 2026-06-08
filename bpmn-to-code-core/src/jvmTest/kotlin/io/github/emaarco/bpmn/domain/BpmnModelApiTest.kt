package io.github.emaarco.bpmn.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BpmnModelApiTest {

    private val processIds = listOf(
        "newsletterSubscription",
        "newsletter-subscription",
        "newsletter_subscription",
    )

    @Test
    fun `fileName returns PascalCase class name regardless of ID separator style`() {

        // given: the expected file name
        val expectedFileName = "NewsletterSubscriptionProcessApi"

        // when / then: all separator variants produce the same file name
        processIds.forEach { id ->
            val model = testBpmnModel(processId = id)
            val api = testBpmnModelApi(model = model)
            assertThat(api.fileName()).isEqualTo(expectedFileName)
        }
    }
}
