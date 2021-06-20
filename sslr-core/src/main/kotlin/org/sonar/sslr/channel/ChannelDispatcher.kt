/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.channel

class ChannelDispatcher<O> private constructor(builder: Builder) : Channel<O>() {
    private val failIfNoChannelToConsumeOneCharacter: Boolean
    private val channels: Array<Channel<O>> = builder.channels.toTypedArray() as Array<Channel<O>>
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

    fun getChannels(): Array<Channel<O>> {
        return channels
    }

    class Builder {
        val channels: MutableList<Channel<*>> = ArrayList()
        var failIfNoChannelToConsumeOneCharacter = false
        fun addChannel(channel: Channel<*>): Builder {
            channels.add(channel)
            return this
        }

        fun addChannels(vararg c: Channel<*>): Builder {
            for (channel in c) {
                addChannel(channel)
            }
            return this
        }

        /**
         * If this option is activated, an IllegalStateException will be thrown as soon as a character won't be consumed by any channel.
         */
        fun failIfNoChannelToConsumeOneCharacter(): Builder {
            failIfNoChannelToConsumeOneCharacter = true
            return this
        }

        fun <O> build(): ChannelDispatcher<O> {
            return ChannelDispatcher(this)
        }
    }

    companion object {
        /**
         * Get a Builder instance to build a new ChannelDispatcher
         */
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    init {
        failIfNoChannelToConsumeOneCharacter = builder.failIfNoChannelToConsumeOneCharacter
    }
}