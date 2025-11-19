package io.github.emaarco.bpmn.adapter.logger

import io.github.emaarco.bpmn.application.port.outbound.LoggerPort

/**
 * No-op logger implementation for testing.
 * Does not output any log messages.
 */
class NoOpLoggerAdapter : LoggerPort {
    override fun debug(message: String) {}
    override fun info(message: String) {}
    override fun warn(message: String) {}
    override fun error(message: String) {}
    override fun error(message: String, throwable: Throwable) {}
}
