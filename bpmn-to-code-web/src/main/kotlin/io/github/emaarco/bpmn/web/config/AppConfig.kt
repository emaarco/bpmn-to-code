package io.github.emaarco.bpmn.web.config

/**
 * Application configuration loaded from environment variables
 */
data class AppConfig(
    val legalLinks: LegalLinksConfig,
    val cors: CorsConfig,
    val port: Int,
) {

    companion object {
        fun fromEnvironment() = AppConfig(
            legalLinks = LegalLinksConfig.fromEnvironment(),
            cors = CorsConfig.fromEnvironment(),
            port = 8080,
        )
    }

}
