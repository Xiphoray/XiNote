import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

new_init = """    private fun initModel() {
        Thread {
            try {
                org.vosk.LibVosk.setLogLevel(org.vosk.LogLevel.INFO)
                
                val assetManager = context.assets
                val externalFilesDir = context.getExternalFilesDir(null) ?: throw Exception("External files dir is null")
                val targetDir = java.io.File(externalFilesDir, "model")
                val resultPathDir = java.io.File(targetDir, "model-cn")
                
                // Always clean and copy to be absolutely sure
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
                // Read logcat
                var logStr = "Logs:\\n"
                try {
                    val process = Runtime.getRuntime().exec("logcat -d -v time")
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                    val logs = mutableListOf<String>()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("VOSK") == true || line?.contains("kaldi") == true || line?.contains("Vosk") == true) {
                            logs.add(line!!)
                        }
                    }
                    logStr += logs.takeLast(10).joinToString("\\n")
                } catch (ignored: Exception) {}
                
                val fullError = "${e.message}\\n$logStr"
                modelError = fullError
                android.util.Log.e("VoskSTT", "Error initializing model: ", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    // Show a Toast but we need more space, so we just set modelError and it will show on click
                    android.widget.Toast.makeText(context, "Vosk Model Error, click mic for details", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }"""

code = re.sub(r'    private fun initModel\(\) \{.*?\n    \}', new_init, code, flags=re.DOTALL)

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "w") as f:
    f.write(code)
