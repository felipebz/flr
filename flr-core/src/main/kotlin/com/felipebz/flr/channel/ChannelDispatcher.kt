/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
 * mailto:felipe AT felipezorzo DOT com DOT br
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.felipebz.flr.channel

public class ChannelDispatcher<O> private constructor(builder: Builder<O>) :
    Channel<O> {
    private val failIfNoChannelToConsumeOneCharacter = builder.failIfNoChannelToConsumeOneCharacter
    public val channels: Array<Channel<O>> = builder.channels.toTypedArray()
    override fun consume(code: CodeReader, output: O): Boolean {
        var nextChar = code.peek()
        while (nextChar != -1) {
            var characterConsumed = false
            for (channel in channels) {
                if (channel.consume(code, output)) {
                    characterConsumed = true
                    break
                }
            }
            if (!characterConsumed) {
                if (failIfNoChannelToConsumeOneCharacter) {
                    val message = ("None of the channel has been able to handle character '" + code.peek()
                        .toChar() + "' (decimal value "
                            + code.peek() + ") at line " + code.getLinePosition() + ", column " + code.getColumnPosition())
                    throw IllegalStateException(message)
                }
                code.pop()
            }
            nextChar = code.peek()
        }
        return true
    }

    public class Builder<O> {
        public val channels: MutableList<Channel<O>> = mutableListOf()
        public var failIfNoChannelToConsumeOneCharacter: Boolean = false
        public fun addChannel(channel: Channel<O>): Builder<O> {
            channels.add(channel)
            return this
        }

        public fun addChannels(vararg c: Channel<O>): Builder<O> {
            for (channel in c) {
                addChannel(channel)
            }
            return this
        }

        /**
         * If this option is activated, an IllegalStateException will be thrown as soon as a character won't be consumed by any channel.
         */
        public fun failIfNoChannelToConsumeOneCharacter(): Builder<O> {
            failIfNoChannelToConsumeOneCharacter = true
            return this
        }

        public fun build(): ChannelDispatcher<O> {
            return ChannelDispatcher(this)
        }
    }

    public companion object {
        /**
         * Get a Builder instance to build a new ChannelDispatcher
         */
        @JvmStatic
        public fun <O>builder(): Builder<O> {
            return Builder()
        }
    }
}