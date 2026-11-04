plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version("2.2.21")

    id("dev.architectury.loom") version("1.11.458") apply false
    id("architectury-plugin") version("3.4.164") apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    version = "${project.property("modCobblemonVersion")!!}-${project.property("modMyVersion")!!}"
    group = project.property("maven_group")!!

    repositories {
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

