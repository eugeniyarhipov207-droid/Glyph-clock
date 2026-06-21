package com.example.ui.glyph

import android.content.Context
import android.util.Log
import com.nothing.sdk.glyph.GlyphFrame
import com.nothing.sdk.glyph.GlyphManager
import com.nothing.sdk.glyph.GlyphSession

class GlyphHelper(private val context: Context) {
    private var glyphManager: GlyphManager? = null
    private var glyphSession: GlyphSession? = null
    private var isConnected = false

    init {
        try {
            glyphManager = GlyphManager.getInstance(context)
            glyphManager?.init(object : GlyphManager.Callback {
                override fun onServiceConnected() {
                    isConnected = true
                    Log.d("GlyphHelper", "Nothing Glyph service is connected successfully!")
                    try {
                        glyphSession = glyphManager?.openSession()
                        glyphSession?.init()
                    } catch (e: Exception) {
                        Log.e("GlyphHelper", "Error opening Glyph Session: ${e.message}")
                    }
                }

                override fun onServiceDisconnected() {
                    isConnected = false
                    Log.w("GlyphHelper", "Nothing Glyph service disconnected.")
                    glyphSession = null
                }
            })
        } catch (e: Exception) {
            Log.e("GlyphHelper", "Glyph API is fallback or not supported: ${e.message}")
        }
    }

    /**
     * Trigger a glowing indicator pattern around physical LED channels.
     */
    fun triggerAlarmPattern() {
        if (!isConnected || glyphSession == null) {
            Log.w("GlyphHelper", "Cannot trigger alarm pattern: Glyph SDK is not connected.")
            return
        }

        try {
            // Build simple flash pattern frames
            val frame = GlyphFrame.Builder()
                .buildChannelA()
                .buildChannelC()
                .buildChannelE()
                .build()

            glyphSession?.playFrame(frame)
            Log.d("GlyphHelper", "Triggered standard flashing alarm pattern via Glyph SDK.")
        } catch (e: Exception) {
            Log.e("GlyphHelper", "Failed to play Glyph pattern: ${e.message}")
        }
    }

    /**
     * Stop and cleanup the active GlyphSession.
     */
    fun stopAndRelease() {
        try {
            glyphSession?.release()
            glyphSession = null
            Log.d("GlyphHelper", "Glyph session released clean.")
        } catch (e: Exception) {
            Log.e("GlyphHelper", "Error releasing Glyph: ${e.message}")
        }
    }
}
