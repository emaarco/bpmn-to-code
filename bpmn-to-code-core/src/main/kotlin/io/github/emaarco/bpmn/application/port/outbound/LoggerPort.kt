package io.github.emaarco.bpmn.application.port.outbound

/**
 * Port for logging operations.
 * Allows the core module to log messages without depending on a specific logging framework.
 * Each adapter (Gradle, Maven, Web) provides its own implementation.
 */
interface LoggerPort {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)
}
