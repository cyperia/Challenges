plugins {
  id("java-library")
  alias(libs.plugins.shadow)
}

group = "net.cyperia.challenges"

dependencies {
  compileOnly(libs.slf4j.api)

  compileOnly(libs.paper.api)
  compileOnly(libs.protocol.lib)

  compileOnly(libs.jetbrains.annotations)
  compileOnly(libs.lombok)

  // gson is already bundled by mojang, accessing it saves on artifact size but might cause compatibilty issues
  // consider bundling relocated impl if version compatibility becomes unmanageable
  compileOnly(libs.gson)

  annotationProcessor(libs.lombok)

  api(project(":cloud-support:api"))
  implementation(project(":catalog"))

  // TODO abstract to register dynamically
  implementation(project(":cloud-support:cloudnet2"))
  implementation(project(":cloud-support:cloudnet3"))
  implementation(project(":cloud-support:cloudnet4"))
  implementation(project(":cloud-support:spawnery"))

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.mockito.core)
  testImplementation(libs.paper.api)
  testRuntimeOnly(libs.protocol.lib)
}

tasks.test {
  useJUnitPlatform()
  // The open-source release is built without the beta classes its catalog still names
  systemProperty("challenges.publicRelease", project.hasProperty("publicRelease"))
}

tasks {

  jar {
    archiveClassifier = "plain"
  }

  shadowJar {
    archiveBaseName = "Challenges"
    archiveClassifier = ""
    archiveVersion = ""

    // The plugin uses Mojang's names already: an older Paper would remap it otherwise, and fails on Java 25 classes
    manifest {
      attributes["paperweight-mappings-namespace"] = "mojang"
    }

    // no explicit dependencies block - bundle "implementation" deps by default
  }

  build {
    dependsOn(shadowJar)
  }
}


val localBuild = file("local.gradle.kts")
if (localBuild.exists()) {
  apply(from = localBuild)
}
