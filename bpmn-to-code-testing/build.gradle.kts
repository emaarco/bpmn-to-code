plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

repositories {
    mavenCentral()
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

dependencies {
    implementation(project(":bpmn-to-code-core"))
    api(libs.assertj)
    compileOnly(libs.junit)
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

mavenPublishing {

    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.emaarco", "bpmn-to-code-testing", version.toString())

    pom {
        name.set("bpmn-to-code-testing")
        description.set("Test utilities for validating BPMN process models — like ArchUnit but for BPMN")
        inceptionYear.set("2026")
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
