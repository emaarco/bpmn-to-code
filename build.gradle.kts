import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt) apply false
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(plugin = "dev.detekt")

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    configure<DetektExtension> {
        autoCorrect = true
        buildUponDefaultConfig = true
        parallel = true
        allRules = false
        debug = true
        ignoreFailures = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
        reports {
            html.required.set(true)
        }
    }
}

// Convenience task to run detekt on all modules
tasks.register("detektAll") {
    group = "verification"
    description = "Run Detekt analysis on all modules"
    dependsOn(subprojects.map { it.tasks.named("detekt") })
}
