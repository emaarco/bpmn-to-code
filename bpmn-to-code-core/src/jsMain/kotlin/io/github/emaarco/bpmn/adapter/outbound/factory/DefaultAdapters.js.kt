package io.github.emaarco.bpmn.adapter.outbound.factory

import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessJsonFileSaver
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.GenerateApiCodePort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort

/**
 * bpmn-moddle parses asynchronously, but [ExtractBpmnPort.extract] is synchronous. Kotlin/JS therefore
 * does not implement the sync extract port — use the suspend `ZeebeBpmnParser` instead and feed the
 * resulting models into the synchronous `ProcessApiGeneration` core.
 */
internal actual fun defaultExtractBpmnPort(): ExtractBpmnPort =
  throw UnsupportedOperationException("Kotlin/JS parses BPMN asynchronously; use the suspend parser instead.")

internal actual fun defaultLoadBpmnFilesPort(): LoadBpmnFilesPort = BpmnFileLoader()

internal actual fun defaultSaveProcessApiPort(): SaveProcessApiPort = ProcessApiFileSaver()

internal actual fun defaultSaveProcessJsonPort(): SaveProcessJsonPort = ProcessJsonFileSaver()

internal actual fun defaultGenerateApiCodePort(): GenerateApiCodePort = CodeGenerationAdapter()
