package io.github.emaarco.bpmn.web.config

import kotlinx.serialization.Serializable

@Serializable
data class LegalLinksConfig(
    val imprintUrl: String?,
    val privacyUrl: String?
) {
    companion object {
        fun fromEnvironment(): LegalLinksConfig {
            val imprintUrl = System.getenv("IMPRINT_URL")?.takeIf { it.isNotBlank() }
            val privacyUrl = System.getenv("PRIVACY_URL")?.takeIf { it.isNotBlank() }
            return LegalLinksConfig(imprintUrl = imprintUrl, privacyUrl = privacyUrl)
        }
    }
}