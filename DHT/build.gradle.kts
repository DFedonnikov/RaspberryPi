plugins {
    kotlin("jvm") version "1.7.10"
    id("io.ktor.plugin") version "2.2.3"
}

group = "me.dmitryfedonnikov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("DHTKt")
}