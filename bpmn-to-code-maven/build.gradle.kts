plugins {
    `maven-publish`
    `kotlin-dsl`
    signing
    alias(libs.plugins.mavenPluginDevelopment)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.emaarco"
version = "0.0.16"

val deps: Configuration by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.bpmnmodel)
    api(libs.bundles.codegen)
    api(libs.ant)
    compileOnly(project(":bpmn-to-code-core"))
    implementation(libs.mavenPluginApi)
    implementation(libs.mavenPluginAnnotations)
    testImplementation(libs.bundles.testing)
}

mavenPlugin {
    dependencies = deps
}

// Configure the jar task to include output from the core module.
tasks.jar {
    from(project(":bpmn-to-code-core").sourceSets.main.get().output)
}

// Create a sources jar (required for Maven Central).
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

// Attach the sources JAR as an additional artifact.
artifacts {
    add("archives", sourcesJar)
}

mavenPublishing {

    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.emaarco", "bpmn-to-code-maven", version.toString())

    // Configure the POM details.
    pom {
        name.set("bpmn-to-code-maven")
        description.set("Maven plugin that bridges gaps between BPMN and code - fostering the creation of clean process-automation solutions")
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

signing {
    val signingKeyId = System.getenv("SIGNING_KEY_ID")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    val signingKey = System.getenv("SIGNING_KEY")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
}