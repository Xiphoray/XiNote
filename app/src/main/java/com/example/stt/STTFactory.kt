package com.example.stt

import android.content.Context

object STTFactory {
    fun createProvider(context: Context): STTProvider {
        // Return native STT by default.
        // Can be easily switched to VoskSTTProvider in the future based on user settings.
        return NativeSTTProvider(context)
    }
}
