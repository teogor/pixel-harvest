plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "dev.teogor"
version = "1.0.0-alpha001"

repositories {
    mavenCentral()
}

application {
    // Specify the main class for your bot
    mainClass.set("dev.teogor.MainKt")
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.drewnoakes:metadata-extractor:2.18.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // database
    implementation("org.jetbrains.exposed:exposed-core:0.31.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.31.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.31.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0")

    // logging
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.9")
}

kotlin {
    jvmToolchain(11)
}