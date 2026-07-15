import re

with open("app/build.gradle.kts", "r") as f:
    code = f.read()

new_task = """    @org.gradle.api.tasks.TaskAction
    fun download() {
        val targetDir = destDir.get().asFile
        val finalMdl = java.io.File(targetDir, "am/final.mdl")
        
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
    }"""

code = re.sub(r'    @org\.gradle\.api\.tasks\.TaskAction\n    fun download\(\) \{.*?\n    \}', new_task, code, flags=re.DOTALL)

with open("app/build.gradle.kts", "w") as f:
    f.write(code)
