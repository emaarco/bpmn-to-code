package io.github.emaarco.bpmn.adapter.logger

import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import org.apache.maven.plugin.logging.Log

/**
 * Adapter that implements LoggerPort using Maven's native logger.
 * Maps log levels to Maven's logging levels.
 */
class MavenLoggerAdapter(
    private val mavenLog: Log
) : LoggerPort {

    override fun debug(message: String) {
        mavenLog.debug(message)
    }

    override fun info(message: String) {
        mavenLog.info(message)
    }

    override fun warn(message: String) {
        mavenLog.warn(message)
    }

    override fun error(message: String) {
        mavenLog.error(message)
    }

    override fun error(message: String, throwable: Throwable) {
        mavenLog.error(message, throwable)
    }
}
