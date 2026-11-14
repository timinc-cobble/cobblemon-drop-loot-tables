rootProject.name = "droploottables"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

include("common")

// Based on env vars, load the respective loaders
// CI var is always present on GitHub CI, LOADER trickles in publish-maven matrix
val isCI = providers.environmentVariable("CI").orNull?.isNotEmpty() ?: false

if (isCI.and(System.getenv("LOADER") == "fabric") || !isCI) {
    include("fabric")
}

if (isCI.and(System.getenv("LOADER") == "neoforge") || !isCI) {
    include("neoforge")
}
