package com.example.stt

import android.content.Context
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.IOException

class VoskSTTProvider(private val context: Context) : STTProvider {
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var resultCallback: ((String) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null
    private var stateCallback: ((Boolean) -> Unit)? = null

    init {
        initModel()
    }

    private var modelError: String? = null

    private fun initModel() {
        Thread {
            try {
                org.vosk.LibVosk.setLogLevel(org.vosk.LogLevel.INFO)
                val assetManager = context.assets
                val externalFilesDir = context.getExternalFilesDir(null) ?: throw Exception("External files dir is null")
                val targetDir = java.io.File(externalFilesDir, "model")
                val resultPathDir = java.io.File(targetDir, "model-cn")
                
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
                var logStr = "Logs:\n"
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
                    logStr += logs.takeLast(10).joinToString("\n")
                } catch (ignored: Exception) {}
                
                val fullError = "${e.message}\n$logStr"
                modelError = fullError
                android.util.Log.e("VoskSTT", "Error initializing model: ", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Vosk Model Error, click mic for details", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (Boolean) -> Unit
    ) {
        this.resultCallback = onResult
        this.errorCallback = onError
        this.stateCallback = onStateChange

        if (modelError != null) {
            onError("Init Error: $modelError")
            return
        }
        if (model == null) {
            onError("Model is loading. Please wait a moment and try again.")
            return
        }
        if (speechService != null) {
            speechService?.stop()
            speechService = null
        }
        try {
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {}
                override fun onResult(hypothesis: String?) {
                    val text = extractText(hypothesis)
                    if (text.isNotBlank()) {
                        resultCallback?.invoke(text.replace(" ", ""))
                    }
                }
                override fun onFinalResult(hypothesis: String?) {
                    val text = extractText(hypothesis)
                    if (text.isNotBlank()) {
                        resultCallback?.invoke(text.replace(" ", ""))
                    }
                    stateCallback?.invoke(false)
                }
                override fun onError(exception: Exception?) {
                    errorCallback?.invoke(exception?.message ?: "Unknown Vosk error")
                    stateCallback?.invoke(false)
                }
                override fun onTimeout() {
                    stateCallback?.invoke(false)
                }
            })
            stateCallback?.invoke(true)
        } catch (e: Throwable) {
            var logStr = "Logs:\n"
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
                logStr += logs.takeLast(10).joinToString("\n")
            } catch (ignored: Exception) {}
            
            onError("Start Error: ${e.message}\n$logStr")
        }
    }

    private fun extractText(hypothesis: String?): String {
        if (hypothesis == null) return ""
        try {
            val obj = org.json.JSONObject(hypothesis)
            return obj.optString("text", "")
        } catch (e: Exception) {
            return ""
        }
    }

    override fun stopListening() {
        speechService?.stop()
        speechService = null
        stateCallback?.invoke(false)
    }

    override fun destroy() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        model?.close()
        model = null
    }
}
