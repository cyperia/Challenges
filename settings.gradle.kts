rootProject.name = "Challenges"

include("plugin", "mongo-connector", "catalog", "catalog-plugin")

includeSubmodules("cloud-support")

fun includeSubmodules(parentDirName: String) {
  val parentDir = File(settingsDir, parentDirName)
  if (parentDir.exists() && parentDir.isDirectory) {
    parentDir.listFiles { file -> file.isDirectory }?.forEach { dir ->
      if (File(dir, "build.gradle").exists() || File(dir, "build.gradle.kts").exists()) {
        include("$parentDirName:${dir.name}")
      }
    }
  }
}
