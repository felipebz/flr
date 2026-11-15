//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
