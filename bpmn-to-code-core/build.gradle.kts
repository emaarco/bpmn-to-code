import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    jacoco
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    js {
        nodejs()
        binaries.executable()
        browser {
            // Karma/browser tests need a real browser; the jsTest suite is Node-only.
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
                // kotlin-logging backs onto slf4j on the JVM.
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
            resources.srcDir(rootProject.file("shared"))
        }
        jsMain {
            dependencies {
                implementation(libs.kotlinxCoroutinesCore)
                // bpmn-moddle 7.1.3 is the last pure-CommonJS release.
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

// The default detekt task only scans src/main|test, which are empty for a KMP module.
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
    "**/adapter/outbound/json/model/**",
    "**/application/port/**",
    "**/*\$DefaultImpls*",
    "**/*\$Companion*",
)

// JaCoCo wiring for the KMP jvm target (the plugin instruments jvmTest into build/jacoco/jvmTest.exec).
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

// npm packaging for the Kotlin/JS CLI: assemble a package + `npm pack` (no live publish).
val assembleNpmPackage by tasks.registering {
    group = "npm"
    description = "Assemble a publishable npm package for the Kotlin/JS CLI (no publish)."
    dependsOn("compileDevelopmentExecutableKotlinJs")
    notCompatibleWithConfigurationCache("Reads the Kotlin/JS node output from the root build dir")

    val sourceKotlinDir = rootProject.layout.buildDirectory
        .dir("js/packages/bpmn-to-code-bpmn-to-code-core/kotlin").get().asFile
    val outDir = layout.buildDirectory.dir("npm").get().asFile
    val packageVersion = version.toString()
    val packageName = "@emaarco/bpmn-to-code"
    outputs.dir(outDir)
    // The compiled JS lives in the root build dir (cross-project), so it can't be declared as a
    // tracked input here; always re-assemble so the package never ships a stale bundle/version.
    outputs.upToDateWhen { false }

    doLast {
        outDir.deleteRecursively()
        sourceKotlinDir.copyRecursively(outDir.resolve("kotlin"), overwrite = true)
        outDir.resolve("bin").mkdirs()
        outDir.resolve("bin/bpmn-to-code.mjs").writeText(
            "#!/usr/bin/env node\nimport './../kotlin/bpmn-to-code-bpmn-to-code-core.mjs'\n"
        )
        outDir.resolve("package.json").writeText(
            """
            |{
            |  "name": "$packageName",
            |  "version": "$packageVersion",
            |  "description": "Generate type-safe API definitions from BPMN process models (Zeebe).",
            |  "type": "module",
            |  "bin": {
            |    "bpmn-to-code": "bin/bpmn-to-code.mjs"
            |  },
            |  "main": "kotlin/bpmn-to-code-bpmn-to-code-core.mjs",
            |  "files": [
            |    "kotlin",
            |    "bin"
            |  ],
            |  "engines": {
            |    "node": ">=18"
            |  },
            |  "dependencies": {
            |    "bpmn-moddle": "7.1.3",
            |    "zeebe-bpmn-moddle": "1.14.0"
            |  },
            |  "keywords": [
            |    "bpmn",
            |    "codegen",
            |    "zeebe",
            |    "camunda"
            |  ],
            |  "license": "MIT",
            |  "homepage": "https://github.com/emaarco/bpmn-to-code",
            |  "repository": {
            |    "type": "git",
            |    "url": "git+https://github.com/emaarco/bpmn-to-code.git"
            |  },
            |  "author": "emaarco"
            |}
            |""".trimMargin()
        )
    }
}

val npmPack by tasks.registering(Exec::class) {
    group = "npm"
    description = "Run `npm pack` on the assembled package (dry-run; produces a tarball)."
    dependsOn(assembleNpmPackage)
    notCompatibleWithConfigurationCache("Runs the external npm CLI")
    workingDir = layout.buildDirectory.dir("npm").get().asFile
    commandLine("npm", "pack")
}
