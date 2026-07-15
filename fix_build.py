import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

# Remove EVERYTHING from the first task definition onwards to clean it up
code = re.split(r'val downloadModelTask =', code)[0]
code = re.split(r'abstract class DownloadAndExtractModelTask', code)[0]

task_code = """
import java.net.URL
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

abstract class DownloadAndExtractModelTask : DefaultTask() {
    @get:org.gradle.api.tasks.OutputDirectory
    abstract val destDir: org.gradle.api.file.DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun download() {
        val targetDir = destDir.get().asFile
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

# Extract the existing imports and the rest of the code
imports = re.findall(r'^import .*', code, re.MULTILINE)
code_without_imports = re.sub(r'^import .*\n', '', code, flags=re.MULTILINE)

# Prepend the new imports
all_imports = "\n".join(imports) + "\nimport java.net.URL\nimport java.io.File\nimport java.io.FileOutputStream\nimport java.util.zip.ZipInputStream\n"

with open("app/build.gradle.kts", "w") as f:
    f.write(all_imports + "\n" + code_without_imports + "\n" + task_code)

