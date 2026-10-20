plugins {
  id("java-library")
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(libs.paper.api)
  implementation(project(":catalog"))
}

tasks {

  jar {
    archiveClassifier = "plain"
  }

  shadowJar {
    archiveBaseName = "ChallengesCatalog"
    archiveClassifier = ""
    archiveVersion = ""
  }

  build {
    dependsOn(shadowJar)
  }
}
