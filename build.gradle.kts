plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version("2.2.20")

    id("dev.architectury.loom") version("1.11-SNAPSHOT") apply false
    id("architectury-plugin") version("3.4-SNAPSHOT") apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    version = "${property("modCobblemonVersion")}-${property("modMyVersion")}"
    group = property("maven_group")!!

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.neoforged.net/releases")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://api.modrinth.com/maven")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    java {
        withSourcesJar()
    }
}

subprojects {
    apply(plugin = "maven-publish")
}

