import java.net.URL
import java.util.zip.ZipInputStream
import java.io.FileOutputStream
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.File



plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.xinote.vqzmx"
    minSdk = 24
    targetSdk = 36
    versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
    versionName = "1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices {
  missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}


// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation(libs.markwon.core)
  implementation(libs.markwon.tables)
  implementation(libs.markwon.strikethrough)
  implementation(libs.markwon.tasklist)
  implementation(libs.markwon.html)
  implementation(libs.markwon.image)
  implementation("net.java.dev.jna:jna:5.2.0@aar")
  implementation("com.alphacephei:vosk-android:0.3.47")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}




abstract class DownloadAndExtractModelTask : DefaultTask() {
    @get:org.gradle.api.tasks.OutputDirectory
    abstract val destDir: org.gradle.api.file.DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun download() {
        val targetDir = destDir.get().asFile
        val finalMdl = File(targetDir, "am/final.mdl")
        
        // Validation: If model exists but is suspiciously small (e.g., < 15MB for final.mdl), it might be corrupted by git lfs or zip extraction.
        if (targetDir.exists() && finalMdl.exists() && finalMdl.length() < 10000000L) {
            println("Model seems corrupted or incomplete (final.mdl size: ${finalMdl.length()} bytes). Re-downloading...")
            targetDir.deleteRecursively()
        }

        if (!targetDir.exists() || targetDir.listFiles()?.isEmpty() == true) {
            println("Downloading and extracting Vosk model...")
            targetDir.parentFile.mkdirs()
            URL("https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip").openStream().use { input ->
                ZipInputStream(input).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val filePath = File(targetDir.parentFile, entry.name)
                        if (!entry.isDirectory) {
                            filePath.parentFile.mkdirs()
                            FileOutputStream(filePath).use { output ->
                                zipIn.copyTo(output)
                            }
                        } else {
                            filePath.mkdirs()
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            val extractedDir = File(targetDir.parentFile, "vosk-model-small-cn-0.22")
            if (extractedDir.exists()) {
                extractedDir.renameTo(targetDir)
            }
            println("Model downloaded and extracted successfully.")
        } else {
            println("Vosk model already exists and seems valid.")
        }
    }
}

val downloadModelTask = tasks.register<DownloadAndExtractModelTask>("downloadVoskModel") {
    destDir.set(file("src/main/assets/model-cn"))
}

tasks.matching { it.name.startsWith("generate") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(downloadModelTask)
}
