plugins {
    kotlin("jvm") version "2.1.0"
}

group = "io.offscale"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.ajalt.clikt:clikt:5.0.2")

    // optional support for rendering markdown in help messages
    implementation("com.github.ajalt.clikt:clikt-markdown:5.0.2")
}

tasks.test {
    useJUnitPlatform()
}