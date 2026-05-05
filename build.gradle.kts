import java.util.Properties

plugins {
    base
    id("dev.architectury.loom") version "1.14-SNAPSHOT" apply false
    id("dev.architectury.loom-no-remap") version "1.14-SNAPSHOT" apply false
}

group = providers.gradleProperty("maven_group").get()
version = providers.gradleProperty("mod_version").get()

fun findRepoRoot(start: File): File {
    var current: File? = start
    while (current != null) {
        if (File(current, "gradle/targets").isDirectory) return current
        current = current.parentFile
    }
    error("Could not locate repository root containing gradle/targets from ${start.path}")
}

val repoRoot = findRepoRoot(rootDir)
val activeMc = providers.gradleProperty("target.mc").orElse("1.21.11").get()
val targetPropsFile = File(repoRoot, "gradle/targets/$activeMc.properties")
require(targetPropsFile.exists()) { "Missing target properties: ${targetPropsFile.path}" }

val targetProps = Properties().apply {
    targetPropsFile.inputStream().use(::load)
}

fun prop(name: String): String = targetProps.getProperty(name)
    ?: error("Missing '$name' in ${targetPropsFile.path}")

val minecraftVersion = prop("minecraft_version")
val javaRelease = if (minecraftVersion.startsWith("26.1")) 25 else 21

extra["minecraft_version"] = minecraftVersion
extra["fabric_loader_version"] = prop("fabric_loader_version")
extra["fabric_api_version"] = prop("fabric_api_version")
extra["modmenu_version"] = prop("modmenu_version")
extra["neoforge_version"] = prop("neoforge_version")
extra["java_release"] = javaRelease
extra["use_no_remap"] = minecraftVersion.startsWith("26.1")

allprojects {
    extra["minecraft_version"] = minecraftVersion
    extra["fabric_loader_version"] = prop("fabric_loader_version")
    extra["fabric_api_version"] = prop("fabric_api_version")
    extra["modmenu_version"] = prop("modmenu_version")
    extra["neoforge_version"] = prop("neoforge_version")
    extra["java_release"] = javaRelease
    extra["use_no_remap"] = minecraftVersion.startsWith("26.1")
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.terraformersmc.com/releases/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}

val targetMatrix = listOf(
    "1.21.1-fabric",
    "1.21.1-neoforge",
    "1.21.4-fabric",
    "1.21.4-neoforge",
    "1.21.11-fabric",
    "1.21.11-neoforge",
    "26.1-fabric",
    "26.1-neoforge"
)

val buildTargetTasks = targetMatrix.map { target ->
    val mc = target.substringBeforeLast('-')
    val loader = target.substringAfterLast('-')
    val taskName = "buildTarget${target.replace(".", "").replace("-", "")}"
    tasks.register<GradleBuild>(taskName) {
        group = "build"
        description = "Builds $target"
        tasks = listOf("clean", ":$loader:jar")
        startParameter.projectProperties = mapOf("target.mc" to mc)
    }
}

tasks.register("buildAllTargets") {
    group = "build"
    description = "Builds all configured Minecraft/loader targets."
    dependsOn(buildTargetTasks)
}

tasks.register<Copy>("collectAllJars") {
    group = "build"
    from(layout.projectDirectory.dir("fabric/build/libs")) { include("*.jar") }
    from(layout.projectDirectory.dir("neoforge/build/libs")) { include("*.jar") }
    into(layout.buildDirectory.dir("finalJars"))
}
