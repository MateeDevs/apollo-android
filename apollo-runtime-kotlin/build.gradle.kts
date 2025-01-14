plugins {
  `java-library`
  kotlin("multiplatform")
}

kotlin {
  @Suppress("ClassName")
  data class iOSTarget(val name: String, val preset: String, val id: String)

  @Suppress("ClassName")
  data class tvOSTarget(val name: String, val preset: String, val id: String)


  val iosTargets = listOf(
      iOSTarget("ios", "iosArm64", "ios-arm64"),
      iOSTarget("iosSim", "iosX64", "ios-x64")
  )

  val tvosTargets = listOf(
      tvOSTarget("tvos", "tvosArm64", "tvos-arm64"),
      tvOSTarget("tvosSim", "tvosX64", "tvos-x64")
  )


  for ((targetName, presetName, id) in iosTargets) {
    targetFromPreset(presets.getByName(presetName), targetName) {
      mavenPublication {
        artifactId = "${project.name}-$id"
      }
    }
  }

  for ((targetName, presetName, id) in tvosTargets) {
    targetFromPreset(presets.getByName(presetName), targetName) {
      mavenPublication {
        artifactId = "${project.name}-$id"
      }
    }
  }

  jvm()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":apollo-api"))
        api(groovy.util.Eval.x(project, "x.dep.okio"))
        api(groovy.util.Eval.x(project, "x.dep.uuid"))
        api(groovy.util.Eval.x(project, "x.dep.kotlin.coroutines"))
      }
    }

    val jvmMain by getting {
      dependsOn(commonMain)
      dependencies {
        api(groovy.util.Eval.x(project, "x.dep.okHttp.okHttp4"))
      }
    }

    val iosMain by getting {
      dependsOn(commonMain)
      dependencies {
      }
    }


    val iosSimMain by getting {
      dependsOn(iosMain)
    }

    val tvosMain by getting {
      dependsOn(iosMain)
    }

    val tvosSimMain by getting {
      dependsOn(tvosMain)
    }


    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(kotlin("test-junit"))
        implementation(groovy.util.Eval.x(project, "x.dep.truth"))
        implementation(groovy.util.Eval.x(project, "x.dep.okHttp.okHttp"))
      }
    }

    val iosSimTest by getting {
      dependsOn(commonTest)
    }
  }
}

tasks.register("iOSSimTest") {
  dependsOn("iosSimTestBinaries")
  doLast {
    val binary = kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosSim").binaries.getTest("DEBUG").outputFile
    exec {
      commandLine = listOf("xcrun", "simctl", "spawn", "iPhone 8", binary.absolutePath)
    }
  }
}
