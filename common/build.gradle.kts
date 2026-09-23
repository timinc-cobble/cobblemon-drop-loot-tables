plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("neoforge", "fabric")
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") { isTransitive = false }

    modImplementation("maven.modrinth:cobblemon-tim-core:${property("tim_core_fabric_version")}")
    modImplementation("maven.modrinth:cobblemon-counter:${property("counter_fabric_version")}")

    annotationProcessor("net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7")
    compileOnly("net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7")
    compileOnly("io.github.llamalad7:mixinextras-common:${property("mixin_extras_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
