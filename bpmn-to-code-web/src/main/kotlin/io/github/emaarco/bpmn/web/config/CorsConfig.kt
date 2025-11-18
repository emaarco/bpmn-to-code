package io.github.emaarco.bpmn.web.config

/**
 * CORS configuration loaded from environment variables
 */
data class CorsConfig(
    val allowedOrigins: List<String>
) {
    companion object {
        /**
         * Loads CORS configuration from environment.
         * Reads ALLOWED_CORS_ORIGINS environment variable which can be:
         * - Empty or "*": Allow all origins (default)
         * - Comma-separated list: "https://example.com,https://app.example.com"
         */
        fun fromEnvironment(): CorsConfig {
            val originsEnv = System.getenv("ALLOWED_CORS_ORIGINS")?.trim()

            val allowedOrigins = when {
                originsEnv.isNullOrEmpty() || originsEnv == "*" -> listOf("*")
                else -> originsEnv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }

            return CorsConfig(allowedOrigins = allowedOrigins)
        }
    }

    /**
     * Whether to allow all origins (wildcard)
     */
    fun allowsAllOrigins(): Boolean = allowedOrigins.contains("*")
}
