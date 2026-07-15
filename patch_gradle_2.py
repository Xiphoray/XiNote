import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

# Remove the old task
code = re.sub(r'val downloadModelTask = tasks\.register\("downloadVoskModel"\).*?\}\n\}\n\ntasks\.matching.*?\}\n\}', '', code, flags=re.DOTALL)

task_code = """
abstract class DownloadAndExtractModelTask : DefaultTask() {
    @get:org.gradle.api.tasks.OutputDirectory
    abstract val destDir: org.gradle.api.file.DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun download() {
        val targetDir = destDir.get().asFile
        if (!targetDir.exists() || targetDir.listFiles()?.isEmpty() == true) {
            println("Downloading and extracting Vosk model...")
            targetDir.parentFile.mkdirs()
            java.net.URL("https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip").openStream().use { input ->
                java.util.zip.ZipInputStream(input).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val filePath = java.io.File(targetDir.parentFile, entry.name)
                        if (!entry.isDirectory) {
                            filePath.parentFile.mkdirs()
                            java.io.FileOutputStream(filePath).use { output ->
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
            val extractedDir = java.io.File(targetDir.parentFile, "vosk-model-small-cn-0.22")
            if (extractedDir.exists()) {
                extractedDir.renameTo(targetDir)
            }
            println("Model downloaded and extracted successfully.")
        }
    }
}

val downloadModelTask = tasks.register<DownloadAndExtractModelTask>("downloadVoskModel") {
    destDir.set(file("src/main/assets/model-cn"))
}

tasks.matching { it.name.startsWith("generate") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(downloadModelTask)
}
"""

with open("app/build.gradle.kts", "w") as f:
    f.write(code + "\n" + task_code)
