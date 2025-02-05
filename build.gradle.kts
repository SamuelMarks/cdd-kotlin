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

     implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
     implementation("io.ktor:ktor-server-core:2.3.3")
     implementation("io.ktor:ktor-server-netty:2.3.3")
     implementation("io.ktor:ktor-server-content-negotiation:2.3.3")
     implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
     implementation("org.yaml:snakeyaml:1.33")
     implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
    
}

tasks.test {
    useJUnitPlatform()
}