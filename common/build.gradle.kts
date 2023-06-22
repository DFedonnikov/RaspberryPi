plugins {
    kotlin("jvm") version "1.7.10"
}

group = "me.dmitryfedonnikov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    val pi4j = "2.3.0"
    val pi4jKtx = "2.4.0"
    val slf4j = "2.0.6"
    val coroutines = "1.6.4"
    val joda = "2.12.5"
    implementation(kotlin("stdlib"))
    api("com.pi4j:pi4j-ktx:$pi4jKtx") // Kotlin DSL
    api("com.pi4j:pi4j-core:$pi4j")
    api("com.pi4j:pi4j-plugin-pigpio:$pi4j")
    api("com.pi4j:pi4j-plugin-raspberrypi:$pi4j")
    api("com.pi4j:pi4j-plugin-linuxfs:$pi4j")
    api("org.slf4j:slf4j-api:$slf4j")
    api("org.slf4j:slf4j-simple:$slf4j")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    api("joda-time:joda-time:$joda")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}