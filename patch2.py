import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

new_init = """    private fun initModel() {
        Thread {
            try {
                val assetManager = context.assets
                val externalFilesDir = context.getExternalFilesDir(null) ?: throw Exception("External files dir is null")
                val targetDir = java.io.File(externalFilesDir, "model")
                val resultPathDir = java.io.File(targetDir, "model-cn")
                
                // Always clean and copy to be absolutely sure
                resultPathDir.deleteRecursively()
                
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
                
                // Verify
                val fstFile = java.io.File(resultPathDir, "graph/Gr.fst")
                if (!fstFile.exists()) {
                    throw Exception("File not copied: " + fstFile.absolutePath)
                }
                
                val outputPath = resultPathDir.absolutePath
                val m = org.vosk.Model(outputPath)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    this.model = m
                }
            } catch (e: Exception) {
                modelError = e.message ?: "Unknown error"
                android.util.Log.e("VoskSTT", "Error initializing model: ", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Vosk Model Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Error) {
                modelError = e.message ?: "Unknown error"
                android.util.Log.e("VoskSTT", "Error initializing model (Error): ", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Vosk Model Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }"""

code = re.sub(r'    private fun initModel\(\) \{.*?\n    \}', new_init, code, flags=re.DOTALL)

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "w") as f:
    f.write(code)
