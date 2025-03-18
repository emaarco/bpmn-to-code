package io.github.emaarco.bpmn.adapter.outbound.versioning

import io.github.emaarco.bpmn.application.port.outbound.ApiVersioningPort
import java.io.File
import java.util.*

class VersionService(
    private val versionFile: String = "bpmn-to-code.properties"
) : ApiVersioningPort {

    /**
     * Retrieves the current version number for a given BPMN process ID.
     * If no version is found, returns 0.
     */
    override fun getVersion(
        filePath: String,
        processId: String,
    ): Int {
        val file = File("$filePath/$versionFile")
        if (!file.exists()) return 0
        val properties = Properties()
        file.inputStream().use { properties.load(it) }
        return properties.getProperty(processId)?.toIntOrNull() ?: 0
    }

    /**
     * Updates the version number for a given BPMN process ID.
     */
    override fun increaseVersion(
        filePath: String,
        processId: String,
        newVersion: Int,
    ) {
        val file = File("$filePath/$versionFile")
        val properties = Properties()
        if (file.exists()) file.inputStream().use { properties.load(it) }
        properties.setProperty(processId, newVersion.toString())
        file.outputStream().use { properties.store(it, null) }
    }
}
