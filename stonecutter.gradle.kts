import org.gradle.kotlin.dsl.replace
import java.util.Properties

plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.1-fabric"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fabric_api"] = run {
        val props = Properties()
        val file = rootProject.file("gradle/targets/${node.metadata.version}.properties")
        file.inputStream().use(props::load)
        props.getProperty("fabric_api_version")
    }

    replacements {
        string {
            direction = eval(current.version, ">=1.21.11")
            replace("ResourceLocation", "Identifier")
        }
    }
}