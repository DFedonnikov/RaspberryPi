plugins {
    kotlin("jvm") version "1.7.10"
}

group = "me.dmitryfedonnikov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    api("com.pi4j:pi4j-ktx:2.2.1.2") // Kotlin DSL
    api("com.pi4j:pi4j-core:2.2.1")
    api("com.pi4j:pi4j-plugin-pigpio:2.2.1")
    api("com.pi4j:pi4j-plugin-raspberrypi:2.2.1")
    api("org.slf4j:slf4j-api:2.0.5")
    api("org.slf4j:slf4j-simple:2.0.5")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}