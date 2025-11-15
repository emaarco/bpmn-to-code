package io.github.emaarco.bpmn.web.config

/**
 * Application configuration loaded from environment variables
 */
data class AppConfig(
    val legalLinks: LegalLinksConfig
) {

    companion object {
        fun fromEnvironment() = AppConfig(
            legalLinks = LegalLinksConfig.fromEnvironment()
        )
    }

}
