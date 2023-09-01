plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "dev.teogor"
version = "1.0.0-alpha01"

repositories {
    mavenCentral()
}

application {
    // Specify the main class for your bot
    mainClass.set("dev.teogor.MainKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.drewnoakes:metadata-extractor:2.18.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // database
    implementation("org.jetbrains.exposed:exposed-core:0.42.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.42.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.42.1")
    implementation("org.xerial:sqlite-jdbc:3.42.0.1")

    // svg converter
    implementation("org.apache.xmlgraphics:batik-transcoder:1.17")
    implementation("org.apache.xmlgraphics:batik-parser:1.17")
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent:5.2.1")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.2")

    // kord
    implementation("dev.kord:kord-core:0.10.0")
    implementation("dev.kord:kord-common:0.10.0")
    implementation("dev.kord:kord-rest:0.10.0")
    implementation("dev.kord:kord-gateway:0.10.0")

    implementation("com.squareup:kotlinpoet:1.14.2")

    // logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

kotlin {
    jvmToolchain(11)
}