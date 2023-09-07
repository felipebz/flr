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
    open public lateinit var reader: Reader
    public lateinit var output: O
    public lateinit var configuration: CodeReaderConfiguration

    public constructor()
    public constructor(output: O) {
        this.output = output
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