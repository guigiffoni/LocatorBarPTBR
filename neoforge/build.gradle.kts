if (rootProject.extra["use_no_remap"] as Boolean) {
    apply(plugin = "dev.architectury.loom-no-remap")
} else {
    apply(plugin = "dev.architectury.loom")
}

base {
    archivesName.set(
        "${rootProject.property("archives_name")}-neoforge-${project.version}+${rootProject.extra["minecraft_version"]}"
    )
}

loom {
    silentMojangMappingsLicense()
    runs {
        named("client") { client() }
    }
}

sourceSets {
    named("main") {
        java.srcDir(project(":common").file("src/main/java"))
        resources.srcDir(project(":common").file("src/main/resources"))
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.extra["minecraft_version"]}")
    if (!(rootProject.extra["use_no_remap"] as Boolean)) {
        mappings(loom.officialMojangMappings())
    }
    add("neoForge", "net.neoforged:neoforge:${rootProject.extra["neoforge_version"]}")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("version" to project.version))
    }
}

java {
    withSourcesJar()
    val release = rootProject.extra["java_release"] as Int
    sourceCompatibility = JavaVersion.toVersion(release)
    targetCompatibility = JavaVersion.toVersion(release)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(rootProject.extra["java_release"] as Int)
}
