package com.nothing.sdk.glyph

import android.content.Context
import android.util.Log

class GlyphSession(private val context: Context) {
    fun init() {
        Log.i("GlyphSession", "GlyphSession dynamic init.")
    }

    fun playFrame(frame: GlyphFrame) {
        Log.i("GlyphSession", "Playing Glyph Frame: channels = ${frame.activeChannels.joinToString()}")
    }

    fun playPattern(patternId: Int) {
        Log.i("GlyphSession", "Playing Custom Pattern ID: $patternId")
    }

    fun release() {
        Log.i("GlyphSession", "GlyphSession released.")
    }
}
