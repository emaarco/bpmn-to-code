package io.github.emaarco.bpmn.web.config

/**
 * Application configuration loaded from environment variables
 */
data class AppConfig(
    val legalLinks: LegalLinksConfig,
    val cors: CorsConfig
) {

    companion object {
        fun fromEnvironment() = AppConfig(
            legalLinks = LegalLinksConfig.fromEnvironment(),
            cors = CorsConfig.fromEnvironment()
        )
    }

}
