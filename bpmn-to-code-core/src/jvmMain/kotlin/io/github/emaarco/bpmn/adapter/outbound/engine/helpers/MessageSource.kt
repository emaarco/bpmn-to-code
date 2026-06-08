package io.github.emaarco.bpmn.adapter.outbound.engine.helpers

import org.camunda.bpm.model.bpmn.instance.Message

data class MessageSource(val elementId: String?, val name: String?, val message: Message?)
