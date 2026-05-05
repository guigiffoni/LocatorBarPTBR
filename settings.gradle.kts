pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

rootProject.name = "locatorbar"

include("common")
include("fabric")
include("neoforge")

stonecutter {
    create(rootProject) {
        version("1.21.1-fabric", "1.21.1")
        version("1.21.1-neoforge", "1.21.1")
        version("1.21.4-fabric", "1.21.4")
        version("1.21.4-neoforge", "1.21.4")
        version("1.21.11-fabric", "1.21.11")
        version("1.21.11-neoforge", "1.21.11")
        version("26.1-fabric", "26.1")
        version("26.1-neoforge", "26.1")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}
