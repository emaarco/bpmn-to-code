package io.github.emaarco.bpmn.adapter.outbound.factory

import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort

/**
 * Platform-specific default wiring for the outbound adapters that the use-case services
 * fall back to when constructed without explicit dependencies.
 *
 * The JVM actuals (Camunda extractor, java.nio filesystem, KotlinPoet/JavaPoet codegen) live in
 * jvmMain; later targets (JS) provide their own. This keeps the services in commonMain while
 * preserving their no-arg default-constructor ergonomics.
 */
internal expect fun defaultExtractBpmnPort(): ExtractBpmnPort

internal expect fun defaultLoadBpmnFilesPort(): LoadBpmnFilesPort

internal expect fun defaultSaveProcessApiPort(): SaveProcessApiPort

internal expect fun defaultSaveProcessJsonPort(): SaveProcessJsonPort

internal expect fun defaultGenerateApiCodePort(): GenerateApiCodePort
