plugins {
  base
}

allprojects {
  repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://jitpack.io")
    maven("https://maven.canvasmc.io/releases") {
      content { includeGroup("io.canvasmc.canvas") }
    }
  }
}

var javaVersion = libs.versions.java.get()

subprojects {
  apply(plugin = "java-library")

  group = "${project.group}${project.path.replace(":", ".")}" // hacky fix..

  extensions.configure<JavaPluginExtension>("java") {
    withSourcesJar()
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:-removal") // TODO
  }

  tasks.named<ProcessResources>("processResources") {
    filteringCharset = "UTF-8"
    val tokens = mapOf("version" to project.property("pluginVersion").toString())
    filesMatching(listOf("plugin.yml", "config.yml")) {
      expand(tokens)
    }
    from(rootProject.file("LICENSE"), rootProject.file("ADDITIONAL-TERMS"))
  }

}

