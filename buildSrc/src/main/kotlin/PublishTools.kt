import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType.STABLE
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

private const val MODRINTH_PROJECT_ID_PROPERTY = "publish.modrinth.project_id"
private const val CURSEFORGE_PROJECT_ID_PROPERTY = "publish.curseforge.project_id"

fun Project.configureLocatorBarPublishing() {
    configureRootGithubPublishing()

    val loader = name.substringAfterLast('-')
    val target = name.substringBeforeLast('-')
    val loaderTitle = loader.upperCaseFirst()
    val minecraftTitle = mod.prop("mc_title")
    val minecraftTargets = mod.prop("mc_targets")
    val releaseDisplayName = "Release ${mod.version} for $loaderTitle $minecraftTitle"
    val supportedMinecraftVersions = explicitMinecraftVersions(minecraftTargets)
    val supportedMinecraftRange = minecraftVersionRange(minecraftTargets)
    val changelog = providers.fileContents(
        rootProject.layout.projectDirectory.file(".github/changelogs/$target-$loader-changelog.md")
    ).asText

    extensions.configure<ModPublishExtension>("publishMods") {
        file.set(publishJarTaskName(loader).let { taskName ->
            tasks.named<AbstractArchiveTask>(taskName).flatMap { it.archiveFile }
        })
        this.changelog.set(changelog)
        type.set(STABLE)
        version.set("${mod.version}+$minecraftTitle-$loader")
        displayName.set(releaseDisplayName)
        modLoaders.add(loader)

        modrinth {
            accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
            projectId.set(providerPropertyOrEnvironment(MODRINTH_PROJECT_ID_PROPERTY, "MODRINTH_PROJECT_ID"))
            configureMinecraftVersions(supportedMinecraftVersions, supportedMinecraftRange)
            if (loader == "fabric") {
                requires("fabric-api")
                optional("modmenu")
            }
        }

        curseforge {
            accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
            projectId.set(providerPropertyOrEnvironment(CURSEFORGE_PROJECT_ID_PROPERTY, "CURSEFORGE_PROJECT_ID"))
            configureMinecraftVersions(supportedMinecraftVersions, supportedMinecraftRange)
            clientRequired.set(true)
            serverRequired.set(true)
            this.changelog.set(changelog)
            changelogType.set("markdown")
            if (loader == "fabric") {
                requires("fabric-api")
                optional("modmenu")
            }
        }
    }
}

private fun Project.configureRootGithubPublishing() {
    val configuredMarker = "locatorbar.publish.root_configured"
    if (rootProject.extensions.extraProperties.has(configuredMarker)) {
        return
    }
    rootProject.extensions.extraProperties.set(configuredMarker, true)
    rootProject.plugins.apply("me.modmuss50.mod-publish-plugin")

    val modVersion = rootProject.providers.gradleProperty("mod.version")
    rootProject.extensions.configure<ModPublishExtension>("publishMods") {
        changelog.set(
            rootProject.providers.fileContents(
                rootProject.layout.projectDirectory.file(".github/changelogs/matrix-changelog.md")
            ).asText
        )
        type.set(STABLE)
        version.set(modVersion)
        displayName.set(modVersion.map { "release: v$it" })

        github {
            accessToken.set(rootProject.providers.environmentVariable("GITHUB_TOKEN"))
            repository.set(
                rootProject.providers.gradleProperty("publish.github.repository")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REPOSITORY"))
                    .orElse("FuzjaJadrowa/LocatorBar")
            )
            commitish.set(
                rootProject.providers.gradleProperty("publish.github.commitish")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REF_NAME"))
                    .orElse("main")
            )
            tagName.set(modVersion.map { "v$it" })
            allowEmptyFiles.set(true)
        }
    }

    val publishAllMods = rootProject.tasks.register("publishAllMods") {
        group = "publishing"
        description = "Publishes the GitHub release and all versioned Modrinth/CurseForge files."
        dependsOn(rootProject.tasks.named("publishMods"))
    }

    rootProject.gradle.projectsEvaluated {
        val releaseFiles = rootProject.subprojects
            .sortedBy { it.name }
            .map { project ->
                val loader = project.name.substringAfterLast('-')
                project.tasks.named<AbstractArchiveTask>(project.publishJarTaskName(loader)).flatMap { it.archiveFile }
            }

        rootProject.extensions.configure<ModPublishExtension>("publishMods") {
            file.set(releaseFiles.first())
            additionalFiles.from(releaseFiles.drop(1))
        }

        publishAllMods.configure {
            dependsOn(rootProject.subprojects.map { it.tasks.named("publishMods") })
        }
    }
}

private fun Project.publishJarTaskName(loader: String): String {
    return if (loader == "fabric" && tasks.names.contains("remapJar")) {
        "remapJar"
    } else {
        "jar"
    }
}

private fun Project.providerPropertyOrEnvironment(propertyName: String, environmentName: String) =
    providers.gradleProperty(propertyName).orElse(providers.environmentVariable(environmentName))

private fun ModrinthOptions.configureMinecraftVersions(versions: List<String>?, range: Pair<String, String>?) {
    if (versions != null) {
        versions.forEach { minecraftVersions.add(it) }
    } else if (range != null) {
        minecraftVersionRange {
            start.set(range.first)
            end.set(range.second)
            includeSnapshots.set(false)
        }
    }
}

private fun CurseforgeOptions.configureMinecraftVersions(versions: List<String>?, range: Pair<String, String>?) {
    if (versions != null) {
        versions.forEach { minecraftVersions.add(it) }
    } else if (range != null) {
        minecraftVersionRange {
            start.set(range.first)
            end.set(range.second)
        }
    }
}

private fun explicitMinecraftVersions(range: String): List<String>? =
    if (!range.contains(' ') && !range.contains('<') && !range.contains('>')) listOf(range) else null

private fun minecraftVersionRange(range: String): Pair<String, String>? {
    val start = Regex(""">=\s*([^\s]+)""").find(range)?.groupValues?.get(1)
    val end = Regex("""<\s*([^\s]+)""").find(range)?.groupValues?.get(1)
    return if (start != null && end != null) start to end else null
}