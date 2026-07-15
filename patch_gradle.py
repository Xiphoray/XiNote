import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

task_code = """
import java.net.URL
import java.util.zip.ZipInputStream
import java.io.FileOutputStream

val downloadModelTask = tasks.register("downloadVoskModel") {
    val destDir = file("src/main/assets/model-cn")
    val zipFile = file("build/tmp/vosk-model.zip")
    
    outputs.dir(destDir)
    
    doLast {
        if (!destDir.exists() || destDir.listFiles()?.isEmpty() == true) {
            println("Downloading Vosk model...")
            zipFile.parentFile.mkdirs()
            URL("https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip").openStream().use { input ->
                FileOutputStream(zipFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            println("Unzipping Vosk model...")
            destDir.mkdirs()
            copy {
                from(zipTree(zipFile))
                into(destDir.parentFile)
            }
            
            val extractedDir = file("src/main/assets/vosk-model-small-cn-0.22")
            if (extractedDir.exists()) {
                extractedDir.renameTo(destDir)
            }
            println("Model downloaded and extracted successfully.")
        }
    }
}

tasks.matching { it.name.startsWith("generate") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(downloadModelTask)
}
"""

if "downloadVoskModel" not in code:
    with open("app/build.gradle.kts", "a") as f:
        f.write(task_code)
