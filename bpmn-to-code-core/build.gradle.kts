import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    jacoco
}

group = "io.github.emaarco"

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    js {
        nodejs()
        binaries.executable()
        browser {
            // The browser target is kept for the (future) browser bundle, but its tests run via
            // Karma + a real browser. Our jsTest suite is Node-oriented (it uses Node `fs`), so we
            // only execute tests on Node and skip the browser test task.
            testTask {
                enabled = false
            }
        }
        useEsModules()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinxSerializationJson)
                api(libs.kotlinLoggingMultiplatform)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.bpmnmodel)
                // kotlin-logging backs onto slf4j on the JVM; provide the binding API.
                api(libs.slf4jApi)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.bundles.testing)
                implementation(libs.konsist)
                implementation(kotlin("compiler-embeddable"))
                runtimeOnly(libs.junitPlatformLauncher)
            }
            // Shared BPMN fixtures used by the golden tests.
            resources.srcDir(rootProject.file("shared"))
        }
        jsMain {
            dependencies {
                implementation(libs.kotlinxCoroutinesCore)
                // bpmn-moddle 7.1.3 is the last pure-CommonJS release; the Camunda-maintained
                // zeebe moddle extension supplies the Zeebe namespace.
                implementation(npm("bpmn-moddle", "7.1.3"))
                implementation(npm("zeebe-bpmn-moddle", "1.14.0"))
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinxCoroutinesTest)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Run detekt against the multiplatform source sets (the default `detekt` task only
// looks at the conventional src/main|test which are empty for a KMP module).
detekt {
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/jvmMain/kotlin",
        )
    )
}

private val coverageExclusions = listOf(
    "**/domain/shared/**",
    "**/domain/validation/model/**",
    "**/adapter/outbound/engine/constants/**",
    "**/adapter/outbound/engine/extractor/*ImplementationKind*",
    "**/adapter/outbound/factory/**",
    "**/adapter/outbound/json/model/**",
    "**/application/port/**",
    "**/*\$DefaultImpls*",
    "**/*\$Companion*",
)

// JaCoCo wiring for the KMP jvm target. The jacoco plugin instruments the `jvmTest`
// task automatically (producing build/jacoco/jvmTest.exec); we point the report and
// verification at the jvm main compilation output and the moved source sets.
private val jvmMainClasses = layout.buildDirectory.dir("classes/kotlin/jvm/main")
private val jvmExecData = layout.buildDirectory.file("jacoco/jvmTest.exec")
private val coverageSources = files(
    "src/commonMain/kotlin",
    "src/jvmMain/kotlin",
)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("jvmTest")
    executionData(jvmExecData)
    sourceDirectories.setFrom(coverageSources)
    classDirectories.setFrom(
        files(fileTree(jvmMainClasses) { exclude(coverageExclusions) })
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jvmTest")
    executionData(jvmExecData)
    sourceDirectories.setFrom(coverageSources)
    classDirectories.setFrom(
        files(fileTree(jvmMainClasses) { exclude(coverageExclusions) })
    )
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
}
