plugins {
    `java-library`
    id("net.fabricmc.fabric-loom-companion") version "1.14-SNAPSHOT"
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
