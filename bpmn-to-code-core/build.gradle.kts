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

// --- npm packaging (Kotlin/JS CLI, dry-run only — no live publish) ---------------------------
// Assembles a publishable npm package from the compiled Kotlin/JS node output and runs
// `npm pack` to produce a tarball. There is deliberately NO live `npm publish` task here;
// real publishing happens only through the approval-gated CI workflow.
val assembleNpmPackage by tasks.registering {
    group = "npm"
    description = "Assemble a publishable npm package for the Kotlin/JS CLI (no publish)."
    dependsOn("compileDevelopmentExecutableKotlinJs")
    // The Kotlin/JS node output lives under the ROOT project build dir; reading it cross-project
    // is not compatible with the configuration cache. This task is outside the main build path.
    notCompatibleWithConfigurationCache("Reads the Kotlin/JS node output from the root build dir")

    val sourceKotlinDir = rootProject.layout.buildDirectory
        .dir("js/packages/bpmn-to-code-bpmn-to-code-core/kotlin").get().asFile
    val outDir = layout.buildDirectory.dir("npm").get().asFile
    val packageVersion = version.toString()
    val packageName = "@emaarco/bpmn-to-code"
    outputs.dir(outDir)

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
