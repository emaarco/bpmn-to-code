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
    implementation(libs.ant)
    api(libs.slf4jApi)
    api(libs.kotlinLogging)
    testImplementation(libs.bundles.testing)
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
