import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
}

group = "nl.cargoledger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.helger.ubl", "ph-ubl23", "6.7.0")
    implementation("com.google.code.gson", "gson", "2.10")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}