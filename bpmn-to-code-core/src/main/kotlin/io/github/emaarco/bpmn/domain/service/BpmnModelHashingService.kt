package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import java.security.MessageDigest

object BpmnModelHashingService {

    fun BpmnModel.calculateHash(): String {
        val content = this.toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

}
