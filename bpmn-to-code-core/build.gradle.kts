plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.seriazliation)
}

group = "io.github.emaarco"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.bpmnmodel)
    implementation(libs.bundles.codegen)
    implementation(libs.kotlinJson)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
