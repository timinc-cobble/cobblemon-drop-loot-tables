plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("neoforge", "fabric")
}

loom {
    silentMojangMappingsLicense()

    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp = true
        defaultRefmapName.set("mixins.${rootProject.name}.${project.name}.refmap.json")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") { isTransitive = false }

    annotationProcessor("net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7")
    compileOnly("net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")

    modImplementation("us.timinc.mc.cobblemon.timcore:common:${property("tim_core_version")}") { isTransitive = false }
    modImplementation("us.timinc.mc.cobblemon.counter:common:${property("counter_version")}") { isTransitive = false }
}

tasks {
    remapJar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }

    remapSourcesJar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }
}
