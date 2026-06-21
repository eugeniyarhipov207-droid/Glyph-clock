package com.nothing.sdk.glyph

class GlyphFrame(val activeChannels: List<Int>) {
    class Builder {
        private val channels = mutableListOf<Int>()

        fun buildChannel(channel: Int): Builder {
            channels.add(channel)
            return this
        }

        fun buildChannelA(): Builder = buildChannel(1)
        fun buildChannelB(): Builder = buildChannel(2)
        fun buildChannelC(): Builder = buildChannel(3)
        fun buildChannelD(): Builder = buildChannel(4)
        fun buildChannelE(): Builder = buildChannel(5)

        fun build(): GlyphFrame {
            return GlyphFrame(channels)
        }
    }
}
