plugins {
  id("java-library")
  alias(libs.plugins.shadow)
}

group = "net.cyperia.challenges"

// Canvas-only classes. Compiled against canvas-api, which the plugin never touches unless the server
// is Canvas (see RespawnPortalAdapters), so the plugin still loads on Paper and Purpur.
val canvas: SourceSet by sourceSets.creating {
  compileClasspath += sourceSets.main.get().output
}

// The Canvas adapter runs against canvas-api only: its classes are a superset of paper-api, which would
// shadow them if both were on the classpath.
val canvasTest: SourceSet by sourceSets.creating {
  compileClasspath += canvas.output + sourceSets.main.get().output
  runtimeClasspath += canvas.output + sourceSets.main.get().output
}

dependencies {
  compileOnly(libs.slf4j.api)

  compileOnly(libs.paper.api)
  compileOnly(libs.protocol.lib)
  "canvasCompileOnly"(libs.canvas.api)

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

  "canvasTestImplementation"(libs.canvas.api)
  "canvasTestImplementation"(platform(libs.junit.bom))
  "canvasTestImplementation"(libs.junit.jupiter)
  "canvasTestImplementation"(libs.mockito.core)
  "canvasTestImplementation"(libs.slf4j.api)
  "canvasTestImplementation"(libs.gson)
  "canvasTestRuntimeOnly"(libs.junit.platform.launcher)
}

val canvasTestTask = tasks.register<Test>("canvasTest") {
  description = "Runs the Canvas adapter tests against canvas-api."
  group = "verification"
  testClassesDirs = canvasTest.output.classesDirs
  classpath = canvasTest.runtimeClasspath
  useJUnitPlatform()
}

tasks.check {
  dependsOn(canvasTestTask)
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

    from(canvas.output)
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
