plugins {
  id("java-library")
}

dependencies {
  compileOnly(libs.spigot.api)
}

tasks {
  jar {
    archiveClassifier = "plain"
  }
}
