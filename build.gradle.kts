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
    // For tests Junit4
    testImplementation("junit:junit:4.13.2")

    // For mockito
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    // For Mockk
    testImplementation("io.mockk:mockk:1.13.7")

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
    useJUnit() // JUnit4 if JUnit5 -> usejUnitPlatform()
}