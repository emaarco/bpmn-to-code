package io.github.emaarco.bpmn.domain

import java.io.InputStream

data class BpmnResource(
    val fileName: String,
    val content: InputStream,
)
