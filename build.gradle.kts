import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.intershop.gradle.jaxb") version "5.2.1"
}

group = "nl.cargoledger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.helger.ubl", "ph-ubl23", "6.7.0")
    implementation("com.google.code.gson", "gson", "2.10")
    implementation("jakarta.xml.bind", "jakarta.xml.bind-api", "4.0.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

jaxb {
    javaGen {
        register("uncefact") {
            schema = file("src/main/resources/uncefact/eCMR_131pD22A.xsd")
//            binding = file("binding.xjb")
        }
    }
}