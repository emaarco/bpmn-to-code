package io.github.emaarco.bpmn.adapter.outbound.codegen

import io.github.emaarco.bpmn.adapter.outbound.codegen.builder.JavaApiBuilder
import io.github.emaarco.bpmn.adapter.outbound.codegen.builder.KotlinApiBuilder
import io.github.emaarco.bpmn.application.port.outbound.WriteApiFilePort
import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.shared.OutputLanguage

class WriteApiFileAdapter(
    private val builder: Map<OutputLanguage, ApiFileBuilder> = Companion.builder
) : WriteApiFilePort {

    override fun writeApiFile(modelApi: BpmnModelApi) {
        val outputLanguage = modelApi.outputLanguage
        val builder = builder[outputLanguage] ?: throw IllegalArgumentException("$outputLanguage is not supported")
        builder.buildApiFile(modelApi)
    }

    interface ApiFileBuilder {
        fun buildApiFile(modelApi: BpmnModelApi)
    }

    companion object {
        val builder = mapOf(
            OutputLanguage.KOTLIN to KotlinApiBuilder(),
            OutputLanguage.JAVA to JavaApiBuilder()
        )
    }
}