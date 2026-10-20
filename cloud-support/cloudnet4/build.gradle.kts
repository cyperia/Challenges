plugins {
  id("java-library")
}

dependencies {
  compileOnly(libs.spigot.api)
  compileOnly(libs.cloudnet4.driver)
  compileOnly(libs.cloudnet4.bridge)
  compileOnly(libs.cloudnet4.wrapper)

  api(project(":cloud-support:api"))
}

tasks {
  jar {
    archiveClassifier = "plain"
  }
}
