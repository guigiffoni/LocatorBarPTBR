import java.util.Properties

plugins {
    id("dev.architectury.loom-no-remaped") version "1.14-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT" apply false
}

val selectedMc = (findProperty("target.mc")?.toString() ?: stonecutter.current.version)
val targetProps = Properties().apply {
    val f = rootProject.file("gradle/targets/$selectedMc.properties")
    require(f.exists()) { "Missing target properties file: ${f.path}" }
    f.inputStream().use(::load)
}

fun tp(key: String): String = targetProps.getProperty(key)
    ?: error("Missing key '$key' in gradle/targets/$selectedMc.properties")

allprojects {
    group = property("mod.group").toString()
    version = "${property("mod.version")}+$selectedMc"
}

subprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.terraformersmc.com/"
        )
    }
}

project(":common") {
    apply(plugin = "dev.architectury.loom-no-remaped")

    dependencies {
        "minecraft"("com.mojang:minecraft:${tp("minecraft_version")}")
        "modImplementation"("net.fabricmc:fabric-loader:${tp("fabric_loader_version")}")
    }
}

project(":fabric") {
    apply(plugin = "dev.architectury.loom-no-remaped")

    base.archivesName.set("${property("mod.id")}-fabric")

    dependencies {
        "minecraft"("com.mojang:minecraft:${tp("minecraft_version")}")
        "modImplementation"("net.fabricmc:fabric-loader:${tp("fabric_loader_version")}")
        "modImplementation"("net.fabricmc.fabric-api:fabric-api:${tp("fabric_api_version")}")
        "modImplementation"("com.terraformersmc:modmenu:${tp("modmenu_version")}")
        "implementation"(project(":common"))
    }

    tasks.processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to rootProject.version.toString()))
        }
    }
}

project(":neoforge") {
    apply(plugin = "dev.architectury.loom-no-remaped")

    base.archivesName.set("${property("mod.id")}-neoforge")

    dependencies {
        "minecraft"("com.mojang:minecraft:${tp("minecraft_version")}")
        "neoForge"("net.neoforged:neoforge:${tp("neoforge_version")}")
        "implementation"(project(":common"))
    }

    tasks.processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to rootProject.version.toString()))
        }
    }
}

subprojects {
    java {
        withSourcesJar()
        val targetJava = if (sc.eval(selectedMc, ">= 26.1")) JavaVersion.VERSION_25 else JavaVersion.VERSION_21
        sourceCompatibility = targetJava
        targetCompatibility = targetJava
    }
}
