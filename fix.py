import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

new_init = """    private fun initModel() {
        Thread {
            var resultPathDir: java.io.File? = null
            try {
                org.vosk.LibVosk.setLogLevel(org.vosk.LogLevel.INFO)
                val assetManager = context.assets
                val externalFilesDir = context.getExternalFilesDir(null) ?: throw Exception("External files dir is null")
                val targetDir = java.io.File(externalFilesDir, "model")
                resultPathDir = java.io.File(targetDir, "model-cn")
                
                if (!resultPathDir.exists()) {
                    fun copyAssetDir(path: String, outPath: java.io.File) {
                        val assets = assetManager.list(path) ?: return
                        if (assets.isEmpty()) {
                            val outFile = java.io.File(outPath, path)
                            outFile.parentFile?.mkdirs()
                            assetManager.open(path).use { input ->
                                java.io.FileOutputStream(outFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } else {
                            val dir = java.io.File(outPath, path)
                            dir.mkdirs()
                            for (asset in assets) {
                                copyAssetDir("$path/$asset", outPath)
                            }
                        }
                    }
                    copyAssetDir("model-cn", targetDir)
                }
                
                val outputPath = resultPathDir.absolutePath
                val m = org.vosk.Model(outputPath)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    this.model = m
                    android.widget.Toast.makeText(context, "Vosk Model Loaded", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                var fileList = ""
                try {
                    val copiedFiles = mutableListOf<String>()
                    fun listFilesRec(dir: java.io.File, prefix: String = "") {
                        dir.listFiles()?.forEach { 
                            if (it.isDirectory) {
                                listFilesRec(it, "$prefix${it.name}/")
                            } else {
                                copiedFiles.add("$prefix${it.name} (${it.length()})")
                            }
                        }
                    }
                    if (resultPathDir?.exists() == true) {
                        listFilesRec(resultPathDir!!)
                        fileList = "Files: " + copiedFiles.joinToString(", ")
                    } else {
                        fileList = "Dir does not exist"
                    }
                } catch(ignored: Exception) {}
            
                var logStr = "Logs:\\n"
                try {
                    val process = Runtime.getRuntime().exec("logcat -d -v time")
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                    val logs = mutableListOf<String>()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("VOSK") == true || line?.contains("kaldi") == true || line?.contains("Vosk") == true || line?.contains("org.vosk") == true) {
                            logs.add(line!!)
                        }
                    }
                    logStr += logs.takeLast(15).joinToString("\\n")
                } catch (ignored: Exception) {}
                
                val fullError = "${e.message}\\n$fileList\\n$logStr"
                modelError = fullError
                android.util.Log.e("VoskSTT", "Error initializing model: ", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Vosk Model Error, click mic for details", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }"""

code = re.sub(r'    private fun initModel\(\) \{.*?\n    \}\n\n    override fun startListening', new_init + '\n\n    override fun startListening', code, flags=re.DOTALL)

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "w") as f:
    f.write(code)
