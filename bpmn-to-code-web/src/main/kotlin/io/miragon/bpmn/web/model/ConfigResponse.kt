package io.miragon.bpmn.web.model

import io.miragon.bpmn.web.config.LegalLinksConfig
import kotlinx.serialization.Serializable

@Serializable
data class ConfigResponse(
    val legalLinks: LegalLinksConfig,
    val version: String,
)
