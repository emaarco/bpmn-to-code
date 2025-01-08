package io.github.emaarco.bpmn.application.port.outbound

interface ApiVersioningPort {
    fun getVersion(filePath: String, processId: String): Int
    fun increaseVersion(filePath: String, processId: String, newVersion: Int)
}