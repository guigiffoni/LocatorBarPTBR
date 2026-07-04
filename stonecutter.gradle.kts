plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1-forge"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
}