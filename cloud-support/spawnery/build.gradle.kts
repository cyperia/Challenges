plugins {
  id("java-library")
}

dependencies {
  compileOnly(libs.spigot.api)
  compileOnly(libs.spawnery.api)

  api(project(":cloud-support:api"))
}

tasks {
  jar {
    archiveClassifier = "plain"
  }
}
