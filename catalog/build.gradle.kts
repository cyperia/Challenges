import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
  id("java-library")
}

group = "net.cyperia.challenges"

dependencies {
  compileOnly(libs.jetbrains.annotations)
  // Paper and Velocity both bundle gson, so a consumer never has to shade it
  compileOnly(libs.gson)
  // Only CatalogExport parses texts, and every platform that loads the catalog bundles adventure
  compileOnly(libs.adventure.minimessage)

  testImplementation(libs.gson)
  testImplementation(libs.adventure.minimessage)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
  useJUnitPlatform()
}

val exportRuntime by configurations.creating

dependencies {
  exportRuntime(libs.gson)
  exportRuntime(libs.adventure.minimessage)
}

tasks.register<JavaExec>("exportCatalog") {
  classpath = sourceSets.main.get().runtimeClasspath + exportRuntime
  mainClass = "net.cyperia.challenges.catalog.CatalogExport"
  args(layout.projectDirectory.file("export/catalog.json").asFile.path)
}

tasks.processResources {
  val tokens = mapOf("version" to project.property("pluginVersion").toString())
  inputs.properties(tokens)
  filesMatching("challenges-catalog/release.properties") {
    expand(tokens)
  }
  from("../language") {
    into("language")
    include("locales/*.json", "languages.json")
  }

  // minify bundled json resources: doLast so we only modify the files AFTER they have been copied
  // to the build/resources directory, leaving original source files intact.
  doLast {
    fileTree(destinationDir).matching {
      include("**/*.json")
    }.forEach { file ->
      file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
    }
  }
}

