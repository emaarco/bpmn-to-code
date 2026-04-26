plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    jacoco
    `java-test-fixtures`
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
    testImplementation(libs.konsist)
    testImplementation(kotlin("compiler-embeddable"))
    testRuntimeOnly(libs.junitPlatformLauncher)
    testFixturesApi(libs.konsist)
    testFixturesApi(libs.junit)
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

private val coverageExclusions = listOf(
    "**/domain/shared/**",
    "**/domain/validation/model/**",
    "**/adapter/outbound/engine/constants/**",
    "**/adapter/outbound/engine/extractor/*ImplementationKind*",
    "**/adapter/outbound/json/model/**",
    "**/application/port/**",
    "**/*\$DefaultImpls*",
    "**/*\$Companion*",
)

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}
