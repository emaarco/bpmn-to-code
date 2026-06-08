plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

repositories {
    mavenCentral()
}

// Shared konsist arch-test base for the plugin wrapper modules; a standalone JVM module because
// `java-test-fixtures` does not compose with the Kotlin Multiplatform core.
dependencies {
    api(libs.konsist)
    api(libs.junit)
}
