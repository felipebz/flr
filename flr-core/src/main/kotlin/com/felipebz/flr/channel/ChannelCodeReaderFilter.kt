//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.channel

import java.io.IOException
import java.io.Reader

/**
 * This class is a special CodeReaderFilter that uses Channels to filter the character stream before it is passed to the main channels
 * declared for the CodeReader.
 *
 */
public class ChannelCodeReaderFilter<O : Any> : CodeReaderFilter<O> {
    private var channels: Array<out Channel<O>> = emptyArray()
    private lateinit var internalCodeReader: CodeReader

    /**
     * Creates a CodeReaderFilter that will use the provided Channels to filter the character stream it gets from its reader.
     *
     * @param channels
     * the different channels
     */
    public constructor(vararg channels: Channel<O>) : super() {
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
    public constructor(output: O, vararg channels: Channel<O>) : super(output) {
        this.channels = channels
    }

    /**
     * {@inheritDoc}
     */
    override var reader: Reader
        get() = super.reader
        set(reader) {
            super.reader = reader
            internalCodeReader = CodeReader(reader, configuration)
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
                if (channel.consume(internalCodeReader, output)) {
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
