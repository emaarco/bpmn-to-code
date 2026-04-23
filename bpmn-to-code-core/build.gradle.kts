plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.mavenPublish)
    jacoco
    `java-test-fixtures`
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

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
    finalizedBy(tasks.jacocoTestReport)
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
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}

mavenPublishing {

    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.emaarco", "bpmn-to-code-core", version.toString())

    pom {
        name.set("bpmn-to-code-core")
        description.set("Core library for bpmn-to-code — generates type-safe API definitions from BPMN process models")
        inceptionYear.set("2025")
        url.set("https://github.com/emaarco/bpmn-to-code")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("emaarco")
                name.set("Marco Schaeck")
                url.set("https://github.com/emaarco")
            }
        }
        scm {
            url.set("https://github.com/emaarco/bpmn-to-code")
            connection.set("scm:git:git://github.com/emaarco/bpmn-to-code.git")
            developerConnection.set("scm:git:ssh://git@github.com/emaarco/bpmn-to-code.git")
        }
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.75".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}
