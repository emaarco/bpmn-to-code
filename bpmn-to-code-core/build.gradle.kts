plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
}

group = "io.github.emaarco"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.bpmnmodel)
    implementation(libs.bundles.codegen)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
