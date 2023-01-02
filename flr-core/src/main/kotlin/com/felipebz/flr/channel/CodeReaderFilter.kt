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

import java.io.IOException
import java.io.Reader

/**
 * This class can be extended to provide filtering capabilities for the CodeReader class. <br></br>
 * The purpose is to filter the character flow before the CodeReader class passes it to the different channels. It is possible to give
 * several filters to a CodeReader: they will be called one after another, following the declaration order in the CodeReader constructor, to
 * sequentially filter the character flow.
 */
public abstract class CodeReaderFilter<O : Any> {
    private lateinit var reader: Reader
    private lateinit var output: O
    private var configuration: CodeReaderConfiguration? = null

    public constructor()
    public constructor(output: O) {
        this.output = output
    }

    /**
     * Returns the reader from which this class reads the character stream.
     *
     * @return the reader
     */
    public fun getReader(): Reader {
        return reader
    }

    /**
     * Sets the reader from which this class will read the character stream.
     *
     * @param reader
     * the reader
     */
    public open fun setReader(reader: Reader) {
        this.reader = reader
    }

    /**
     * Returns the output object.
     *
     * @return the output
     */
    public fun getOutput(): O {
        return output
    }

    /**
     * Sets the output object
     *
     * @param output
     * the output to set
     */
    public fun setOutput(output: O) {
        this.output = output
    }

    /**
     * Returns the configuration used for the CodeReader
     *
     * @return the configuration
     */
    public fun getConfiguration(): CodeReaderConfiguration {
        return checkNotNull(configuration)
    }

    /**
     * Sets the configuration that must be used by the CodeReader
     *
     * @param configuration
     * the configuration to set
     */
    public fun setConfiguration(configuration: CodeReaderConfiguration?) {
        this.configuration = configuration
    }

    /**
     * This method implements the filtering logic, that is:
     *
     *  *
     * get the characters from the reader,
     *  *
     * filter the character flow (and grab more characters from the reader if the filtering removes some),
     *  *
     * and fill the given buffer to its full capacity with the filtered data.
     *
     *
     * @param filteredBuffer
     * the output buffer that must contain the filtered data
     * @param offset
     * the offset to start reading from the reader
     * @param length
     * the number of characters to read from the reader
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @throws IOException
     * If an I/O error occurs
     */
    @Throws(IOException::class)
    public abstract fun read(filteredBuffer: CharArray, offset: Int, length: Int): Int
}