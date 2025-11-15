plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.2.21"
    application
}

group = "io.github.emaarco"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Core dependency - reuse existing logic
    implementation(project(":bpmn-to-code-core"))

    // Ktor server
    implementation("io.ktor:ktor-server-core:3.0.3")
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("io.ktor:ktor-server-cors:3.0.3")
    implementation("io.ktor:ktor-server-call-logging:3.0.3")
    implementation("io.ktor:ktor-server-status-pages:3.0.3")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.15")

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation("io.ktor:ktor-server-test-host:3.0.3")
    testRuntimeOnly(libs.junitPlatformLauncher)
}

application {
    mainClass.set("io.github.emaarco.bpmn.web.ApplicationKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
