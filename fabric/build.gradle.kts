if (rootProject.extra["use_no_remap"] as Boolean) {
    apply(plugin = "dev.architectury.loom-no-remap")
} else {
    apply(plugin = "dev.architectury.loom")
}

base {
    archivesName.set(
        "${rootProject.property("archives_name")}-fabric-${project.version}+${rootProject.extra["minecraft_version"]}"
    )
}

loom {
    silentMojangMappingsLicense()
    mods {
        maybeCreate("locatorbar").apply {
            sourceSet(sourceSets.main.get())
            sourceSet("main", ":common")
        }
    }
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
        modImplementation("net.fabricmc:fabric-loader:${rootProject.extra["fabric_loader_version"]}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.extra["fabric_api_version"]}")
        modCompileOnly("com.terraformersmc:modmenu:${rootProject.extra["modmenu_version"]}")
    } else {
        implementation("net.fabricmc:fabric-loader:${rootProject.extra["fabric_loader_version"]}")
        implementation("net.fabricmc.fabric-api:fabric-api:${rootProject.extra["fabric_api_version"]}")
        compileOnly("com.terraformersmc:modmenu:${rootProject.extra["modmenu_version"]}")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
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
