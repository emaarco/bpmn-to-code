package io.github.emaarco.bpmn.domain

import io.github.emaarco.bpmn.domain.shared.OutputLanguage

data class GeneratedApiFile(
    val fileName: String,
    val packagePath: String,
    val content: String,
    val language: OutputLanguage,
)
