plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active providers.gradleProperty("stonecutter.active").orElse("1.21.11-fabric").get()

stonecutter parameters {
    constants["is_fabric"] = node.project.path.endsWith(":fabric")
    constants["is_neoforge"] = node.project.path.endsWith(":neoforge")
    constants["mc_1211_or_newer"] = current.parsed >= "1.21.11"
    constants["mc_261_or_newer"] = current.parsed >= "26.1"
}
