import re

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "r") as f:
    code = f.read()

new_start = """        try {
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
        } catch (e: Exception) {
            var logStr = ""
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
                logStr = "\\nLogs:\\n" + logs.takeLast(10).joinToString("\\n")
            } catch (ignored: Exception) {}
            onError("Failed to start Vosk: ${e.message}$logStr")
        }"""

code = re.sub(r'        try \{\n            val recognizer = Recognizer\(model, 16000\.0f\).*?        \} catch \(e: IOException\) \{\n            onError\(e\.message \?: "Failed to start Vosk speech service"\)\n        \}', new_start, code, flags=re.DOTALL)

with open("app/src/main/java/com/example/stt/VoskSTTProvider.kt", "w") as f:
    f.write(code)
