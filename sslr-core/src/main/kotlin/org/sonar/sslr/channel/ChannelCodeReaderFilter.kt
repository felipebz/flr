/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package org.sonar.sslr.channel

import java.io.IOException
import java.io.Reader

/**
 * This class is a special CodeReaderFilter that uses Channels to filter the character stream before it is passed to the main channels
 * declared for the CodeReader.
 *
 */
class ChannelCodeReaderFilter<O : Any> : CodeReaderFilter<O> {
    private var channels: Array<out Channel<O>> = emptyArray()
    private lateinit var internalCodeReader: CodeReader

    /**
     * Creates a CodeReaderFilter that will use the provided Channels to filter the character stream it gets from its reader.
     *
     * @param channels
     * the different channels
     */
    constructor(vararg channels: Channel<O>) : super() {
        this.channels = channels
    }

    /**
     * Creates a CodeReaderFilter that will use the provided Channels to filter the character stream it gets from its reader. And optionally,
     * it can push token to the provided output object.
     *
     * @param output
     * the object that may accept tokens
     * @param channels
     * the different channels
     */
    constructor(output: O, vararg channels: Channel<O>) : super(output) {
        this.channels = channels
    }

    /**
     * {@inheritDoc}
     */
    override fun setReader(reader: Reader) {
        super.setReader(reader)
        internalCodeReader = CodeReader(reader, getConfiguration())
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun read(filteredBuffer: CharArray, offset: Int, length: Int): Int {
        var currentOffset = offset
        if (internalCodeReader.peek() == -1) {
            return -1
        }
        val initialOffset = currentOffset
        while (currentOffset < filteredBuffer.size) {
            if (internalCodeReader.peek() == -1) {
                break
            }
            var consumed = false
            for (channel in channels) {
                if (channel.consume(internalCodeReader, getOutput())) {
                    consumed = true
                    break
                }
            }
            if (!consumed) {
                val charRead = internalCodeReader.pop()
                filteredBuffer[currentOffset] = charRead.toChar()
                currentOffset++
            }
        }
        return currentOffset - initialOffset
    }
}