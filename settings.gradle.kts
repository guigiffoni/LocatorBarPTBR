pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
    id("dev.architectury.loom") version "1.14-SNAPSHOT" apply false
    id("dev.architectury.loom-no-remap") version "1.14-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT" apply false
}

stonecutter {
    kotlinController = true
    shared {
        fun mc(loader: String, vararg versions: String) {
            for (version in versions) {
                val buildscript = when {
                    sc.eval(version, ">= 26.1") -> "build-unobfuscated.gradle.kts"
                    else -> "build-obfuscated.gradle.kts"
                }
                version("$version-$loader", version).buildscript(buildscript)
            }
        }
        mc("fabric","1.21.1", "1.21.4", "1.21.11", "26.1")
        mc("neoforge", "1.21.1", "1.21.4", "1.21.11", "26.1")
    }
    create(rootProject)
}

rootProject.name = "LocatorBar"

include(":common", ":fabric", ":neoforge")
project(":common").projectDir = file("src/common")
project(":fabric").projectDir = file("src/fabric")
project(":neoforge").projectDir = file("src/neoforge")