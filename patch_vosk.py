import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

new_init = """    private fun initModel() {
        Thread {
            try {
                val outputPath = org.vosk.android.StorageService.sync(context, "model-cn", "model")
                val m = org.vosk.Model(outputPath)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    this.model = m
                    android.widget.Toast.makeText(context, "Vosk Model Loaded Successfully", android.widget.Toast.LENGTH_SHORT).show()
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
