import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("org.openapi.generator") version "6.1.0"
}

group = "nl.cargoledger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.helger.ubl", "ph-ubl23", "6.7.0")
    implementation("com.squareup.moshi", "moshi-kotlin", "1.14.0")
    implementation("com.squareup.okhttp3", "okhttp", "4.10.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("src/main/resources/opentrip.json")
    outputDir.set("$buildDir/generated")
    skipValidateSpec.set(true)
}

kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("${buildDir}/generated/src/main/kotlin")
    }
}