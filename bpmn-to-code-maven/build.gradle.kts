import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `maven-publish`
    `kotlin-dsl`
    signing
    alias(libs.plugins.mavenPluginDevelopment)
    id("com.vanniktech.maven.publish") version "0.31.0"
}

group = "io.github.emaarco"
version = "0.0.1"

val deps: Configuration by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.bpmnmodel)
    api(libs.bundles.codegen)
    compileOnly(project(":bpmn-to-code-core"))
    implementation("org.apache.maven:maven-plugin-api:3.8.6")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.15.1")
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

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.emaarco", "bpmn-to-code-maven", version.toString())

    // Configure the POM details.
    pom {
        name.set("bpmn-to-code-maven")
        description.set("A Maven plugin to generate API-like representations from BPMN models")
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