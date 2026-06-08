plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

repositories {
    mavenCentral()
}

// Internal (un-published) test-support module: shared konsist architecture-test base
// classes consumed by the plugin wrapper modules (Gradle, Maven, Web, MCP).
// Lives in its own JVM module because `java-test-fixtures` does not compose with the
// Kotlin Multiplatform core.
dependencies {
    api(libs.konsist)
    api(libs.junit)
}
