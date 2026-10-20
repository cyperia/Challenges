plugins {
  id("java-library")
  alias(libs.plugins.shadow)
}

dependencies {
  implementation(libs.slf4j.api)
  implementation(libs.mongodb.driver)

  compileOnly(libs.spigot.api)

  compileOnly(libs.jetbrains.annotations)
  compileOnly(libs.lombok)

  implementation(project(":plugin"))

  annotationProcessor(libs.lombok)
}

tasks {
  jar {
    archiveClassifier = "plain"
  }

  shadowJar {
    archiveBaseName = "Challenges-MongoConnector"
    archiveClassifier = ""
    archiveVersion = ""

    dependencies {
      include(dependency("org.mongodb:mongodb-driver-sync"))
      include(dependency("org.mongodb:mongodb-driver-core"))
      include(dependency("org.mongodb:bson"))
    }
  }

  build {
    dependsOn(shadowJar)
  }
}




