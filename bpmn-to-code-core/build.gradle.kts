plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.emaarco"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.bpmnmodel)
    implementation(libs.bundles.codegen)
    implementation(libs.kotlinxSerializationJson)

    api(libs.slf4jApi)
    api(libs.kotlinLogging)
    testImplementation(libs.bundles.testing)
    testImplementation(kotlin("compiler-embeddable"))
    testRuntimeOnly(libs.junitPlatformLauncher)
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
