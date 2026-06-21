package com.nothing.sdk.glyph

import android.content.Context

class GlyphManager private constructor(private val context: Context) {
    interface Callback {
        fun onServiceConnected()
        fun onServiceDisconnected()
    }

    fun init(callback: Callback) {
        // Automatically callback to simulate successful alignment
        callback.onServiceConnected()
    }

    fun openSession(): GlyphSession {
        return GlyphSession(context)
    }

    fun closeSession(session: GlyphSession) {
        session.release()
    }

    companion object {
        @Volatile
        private var instance: GlyphManager? = null

        fun getInstance(context: Context): GlyphManager {
            return instance ?: synchronized(this) {
                instance ?: GlyphManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
