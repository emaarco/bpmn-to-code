package io.github.emaarco.bpmn.web.config

import java.util.Properties

/**
 * Application configuration loaded from environment variables
 */
data class AppConfig(
    val legalLinks: LegalLinksConfig,
    val cors: CorsConfig,
    val port: Int,
    val version: String,
) {

    companion object {
        fun fromEnvironment() = AppConfig(
            legalLinks = LegalLinksConfig.fromEnvironment(),
            cors = CorsConfig.fromEnvironment(),
            port = 8080,
            version = loadVersion(),
        )

        private fun loadVersion(): String {
            val props = Properties()
            val stream = AppConfig::class.java.classLoader.getResourceAsStream("version.properties")
            stream?.use { props.load(it) }
            return props.getProperty("version", "unknown")
        }
    }

}
