package io.github.emaarco.bpmn.adapter.logger

import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import org.gradle.api.logging.Logger

/**
 * Adapter that implements LoggerPort using Gradle's native logger.
 * Maps log levels to Gradle's logging levels.
 */
class GradleLoggerAdapter(
    private val gradleLogger: Logger
) : LoggerPort {

    override fun debug(message: String) {
        gradleLogger.debug(message)
    }

    override fun info(message: String) {
        gradleLogger.lifecycle(message)
    }

    override fun warn(message: String) {
        gradleLogger.warn(message)
    }

    override fun error(message: String) {
        gradleLogger.error(message)
    }

    override fun error(message: String, throwable: Throwable) {
        gradleLogger.error(message, throwable)
    }
}
