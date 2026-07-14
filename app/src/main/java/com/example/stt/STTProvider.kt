package com.example.stt

interface STTProvider {
    /**
     * Start listening for speech
     * @param onResult Callback for final recognized text
     * @param onError Callback for errors
     * @param onStateChange Callback for state changes (true = listening, false = stopped)
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (Boolean) -> Unit
    )

    /**
     * Stop listening manually
     */
    fun stopListening()

    /**
     * Release resources
     */
    fun destroy()
}
