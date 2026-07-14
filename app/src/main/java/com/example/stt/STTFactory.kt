package com.example.stt

import android.content.Context

object STTFactory {
    fun createProvider(context: Context, engine: Int = 0): STTProvider {
        return if (engine == 1) {
            VoskSTTProvider(context)
        } else {
            NativeSTTProvider(context)
        }
    }
}
