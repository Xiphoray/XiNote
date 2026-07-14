package com.example.stt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class NativeSTTProvider(private val context: Context) : STTProvider {
    private var speechRecognizer: SpeechRecognizer? = null
    private var resultCallback: ((String) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null
    private var stateCallback: ((Boolean) -> Unit)? = null
    private var isListening = false

    private fun initRecognizer() {
        if (speechRecognizer != null) return
        
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    stateCallback?.invoke(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    isListening = false
                    stateCallback?.invoke(false)
                }

                override fun onError(error: Int) {
                    isListening = false
                    stateCallback?.invoke(false)
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> "Error from server"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Didn't understand, please try again."
                    }
                    errorCallback?.invoke(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        resultCallback?.invoke(matches[0])
                    }
                    isListening = false
                    stateCallback?.invoke(false)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            errorCallback?.invoke("Speech recognition is not available on this device")
        }
    }

    override fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (Boolean) -> Unit
    ) {
        this.resultCallback = onResult
        this.errorCallback = onError
        this.stateCallback = onStateChange

        initRecognizer()

        if (speechRecognizer == null) {
            onError("Speech recognition initialization failed")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to start listening")
            isListening = false
            stateCallback?.invoke(false)
        }
    }

    override fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isListening = false
        stateCallback?.invoke(false)
    }

    override fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        speechRecognizer = null
    }
}
