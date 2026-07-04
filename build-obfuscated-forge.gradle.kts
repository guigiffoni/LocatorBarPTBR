import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftTitle = mod.prop("mc_title")
val loader = stonecutter.current.project.substringAfterLast('-')
val minecraftDependency = mod.dep("minecraft.forge")
val javaVersion = mod.prop("java_version")

version = "${mod.version}+$minecraftTitle"
group = mod.group
base {
    archivesName.set("${mod.name}-$loader")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("src/common/src/main/resources"))
        resources.srcDir(rootProject.file("src/forge/src/main/resources"))
    }
}

versionedJavaSources(
    rootProject.file("src/common/src/main/java"),
    rootProject.file("src/forge/src/main/java")
)

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftDependency")
    mappings(loom.officialMojangMappings())
    add("forge", "net.minecraftforge:forge:${mod.dep("minecraft.forge")}-${mod.dep("forge_loader")}")
}

val requiredJava = JavaVersion.toVersion(javaVersion)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.remapJar {
    inputs.file(tasks.jar.get().archiveFile)
    archiveClassifier = null
    dependsOn(tasks.jar)
}

tasks.jar {
    archiveClassifier = "dev"
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }

    rootProject.tasks.register("testClient") {
        group = "project"
        dependsOn(tasks.named("runClient"))
    }

    rootProject.tasks.register("testServer") {
        group = "project"
        dependsOn(tasks.named("runServer"))
    }
}

loom {
    forge {
        mixinConfigs.add("locatorbar.common.mixins.json")
    }
    runs {
        named("client") {
            runDir = project.projectDir.toPath()
                .relativize(rootProject.file("run/${project.name}/client").toPath())
                .toString()
        }
        named("server") {
            runDir = project.projectDir.toPath()
                .relativize(rootProject.file("run/${project.name}/server").toPath())
                .toString()
        }
    }
}

tasks.processResources {
    properties(
        listOf("META-INF/mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "loader" to mod.dep("forge_loader_range"),
        "forge" to mod.dep("forge_version_range")
    )
    properties(
        listOf("*.mixins.json"),
        "java" to javaVersion
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.processResources {
    doLast {
        fileTree(outputs.files.singleFile).matching {
            include("**/*.json")
        }.forEach { file ->
            file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
        }
    }
}

stonecutter {
    constants {
        put("fabric", false)
        put("neoforge", false)
        put("forge", true)
    }
}

configureLocatorBarPublishing()