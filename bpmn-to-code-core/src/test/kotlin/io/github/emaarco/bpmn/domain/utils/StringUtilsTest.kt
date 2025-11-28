package io.github.emaarco.bpmn.domain.utils

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StringUtilsTest {

    @Test
    fun `toUpperSnakeCase converts camelCase to UPPER_SNAKE_CASE`() {
        assertThat("timerAfter3Days".toUpperSnakeCase()).isEqualTo("TIMER_AFTER_3_DAYS")
        assertThat("sendConfirmationMail".toUpperSnakeCase()).isEqualTo("SEND_CONFIRMATION_MAIL")
    }

    @Test
    fun `toUpperSnakeCase handles hyphens and dots`() {
        assertThat("send-confirmation-mail".toUpperSnakeCase()).isEqualTo("SEND_CONFIRMATION_MAIL")
        assertThat("send.confirmation.mail".toUpperSnakeCase()).isEqualTo("SEND_CONFIRMATION_MAIL")
    }

    @Test
    fun `toUpperSnakeCase strips delegate expression syntax`() {
        assertThat("#{sendConfirmationMailDelegate}".toUpperSnakeCase()).isEqualTo("SEND_CONFIRMATION_MAIL_DELEGATE")
        assertThat("#{abortRegistrationDelegate}".toUpperSnakeCase()).isEqualTo("ABORT_REGISTRATION_DELEGATE")
        assertThat("#{sendWelcomeMailDelegate}".toUpperSnakeCase()).isEqualTo("SEND_WELCOME_MAIL_DELEGATE")
    }

    @Test
    fun `toUpperSnakeCase strips Spring EL expression syntax`() {
        assertThat("\${mySpringBean}".toUpperSnakeCase()).isEqualTo("MY_SPRING_BEAN")
        assertThat("\${someService}".toUpperSnakeCase()).isEqualTo("SOME_SERVICE")
    }

    @Test
    fun `toUpperSnakeCase handles mixed patterns`() {
        assertThat("Activity_SendConfirmationMail".toUpperSnakeCase()).isEqualTo("ACTIVITY_SEND_CONFIRMATION_MAIL")
        assertThat("activity-send-mail-v2".toUpperSnakeCase()).isEqualTo("ACTIVITY_SEND_MAIL_V_2")
    }

    @Test
    fun `toUpperSnakeCase handles already uppercase strings`() {
        assertThat("ALREADY_UPPERCASE".toUpperSnakeCase()).isEqualTo("ALREADY_UPPERCASE")
        assertThat("SEND_MAIL".toUpperSnakeCase()).isEqualTo("SEND_MAIL")
    }
}
