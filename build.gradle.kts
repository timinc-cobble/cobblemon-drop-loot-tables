import groovy.util.Node

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version ("2.4.0")

    id("dev.architectury.loom") version ("1.17-SNAPSHOT") apply false
    id("architectury-plugin") version ("3.5-SNAPSHOT") apply false
    id("com.gradleup.shadow") version ("9.6.1") apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    version = "${project.property("modCobblemonVersion")}-${project.property("modMyVersion")}"
    group = project.property("maven_group") as String

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.cobbled-creators.org/releases")
    }

    java {
        withSourcesJar()
    }
}

// Apply general publication configuration
subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "dev.architectury.loom")

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(tasks.getByName("remapJar"))
                artifact(tasks.getByName("remapSourcesJar"))
                groupId = "${group}.droploottables"
                artifactId = project.name

                pom {
                    withXml {
                        val root: Node = asNode()
                        (root.get("dependencies") as List<*>).forEach {
                            root.remove(it as Node)
                        }
                    }
                }
            }
        }

        if (providers.environmentVariable("CCM_BUILD").orNull?.toBoolean() == true) {
            repositories {
                maven {
                    // Set these variables in gh action secrets
                    if (System.getenv("CCM_USER") == null || System.getenv("CCM_PASS") == null) {
                        throw RuntimeException("Did you forget your publication credentials?")
                    }

                    url = uri("https://maven.cobbled-creators.org/releases")
                    credentials {
                        username = System.getenv("CCM_USER").toString()
                        password = System.getenv("CCM_PASS").toString()
                    }
                }
            }
        }
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
