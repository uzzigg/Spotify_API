import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.4"
val exposedVersion = "0.41.1"
val logbackVersion = "1.4.11"

dependencies {

    // --- KTOR CORE ---
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // --- JSON ---
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    // --- PLUGINS ---
    // CallLogging (nombre correcto del artefacto)
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    // StatusPages
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // --- EXPOSED ORM ---
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")

    // --- BASE DE DATOS ---
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.6.0")

    // --- SERIALIZACIÓN ---
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // --- LOGGING ---
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
