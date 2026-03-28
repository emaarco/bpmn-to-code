package io.github.emaarco.bpmn.web.model

import io.github.emaarco.bpmn.web.config.LegalLinksConfig
import kotlinx.serialization.Serializable

@Serializable
data class ConfigResponse(
    val legalLinks: LegalLinksConfig,
    val version: String,
)
