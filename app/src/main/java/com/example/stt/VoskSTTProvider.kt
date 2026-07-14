package com.example.stt

import android.content.Context
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
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
        StorageService.unpack(context, "model-cn", "model",
            { model ->
                this.model = model
            },
            { exception ->
                modelError = exception.message ?: "Unknown error"
                android.util.Log.e("VoskSTT", "Error unpacking model: " + exception.message, exception)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Vosk Model Error: ${exception.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            })
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
            onError("Failed to load model: $modelError")
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
        } catch (e: IOException) {
            onError(e.message ?: "Failed to start Vosk speech service")
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
