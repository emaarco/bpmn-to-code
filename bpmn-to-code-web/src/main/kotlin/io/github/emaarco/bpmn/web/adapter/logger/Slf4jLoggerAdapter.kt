package io.github.emaarco.bpmn.web.adapter.logger

import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import org.slf4j.Logger

/**
 * Adapter that implements LoggerPort using SLF4J.
 * Uses Logback as the implementation for structured JSON logging in production.
 */
class Slf4jLoggerAdapter(
    private val slf4jLogger: Logger
) : LoggerPort {

    override fun debug(message: String) {
        slf4jLogger.debug(message)
    }

    override fun info(message: String) {
        slf4jLogger.info(message)
    }

    override fun warn(message: String) {
        slf4jLogger.warn(message)
    }

    override fun error(message: String) {
        slf4jLogger.error(message)
    }

    override fun error(message: String, throwable: Throwable) {
        slf4jLogger.error(message, throwable)
    }
}
