package io.github.emaarco.bpmn.adapter.outbound.factory

import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort

/** Platform-specific default outbound adapters, used when a use-case service is built without explicit dependencies. */
internal expect fun defaultExtractBpmnPort(): ExtractBpmnPort

internal expect fun defaultLoadBpmnFilesPort(): LoadBpmnFilesPort

internal expect fun defaultSaveProcessApiPort(): SaveProcessApiPort

internal expect fun defaultSaveProcessJsonPort(): SaveProcessJsonPort

internal expect fun defaultGenerateApiCodePort(): GenerateApiCodePort
