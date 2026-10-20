plugins {
  id("java-library")
}

dependencies {
  compileOnly(libs.spigot.api)
  compileOnly(libs.cloudnet2.bridge)

  api(project(":cloud-support:api"))
}

repositories {
  // legacy cloudnet 3 repository, officially "https://repo.cloudnetservice.eu/repository/releases/"
  maven("https://repo.cloudnetservice.eu/releases/")
}

tasks {
  jar {
    archiveClassifier = "plain"
  }
}
