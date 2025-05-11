/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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

import java.io.FilterReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * The CodeBuffer class provides all the basic features required to manipulate a source code character stream. Those features are :
 *
 *  * Read and consume next source code character : pop()
 *  * Retrieve last consumed character : lastChar()
 *  * Read without consuming next source code character : peek()
 *  * Read without consuming character at the specified index after the cursor
 *  * Position of the pending cursor : line and column
 *
 */
public open class CodeBuffer protected constructor(initialCodeReader: Reader, configuration: CodeReaderConfiguration) :
    CharSequence {
    private var lastChar = -1
    private val buffer: CharArray
    private var bufferPosition = 0
    private var tabWidth = 0
    private var recordingMode = false
    private var recordedCharacters = StringBuilder()

    public val cursor: Cursor

    /**
     * Note that this constructor will read everything from reader and will close it.
     */
    init {
        /* Make sure the reader passed-in gets closed when done. */
        try {
            initialCodeReader.use { reader ->
                lastChar = -1
                cursor = Cursor()
                tabWidth = configuration.getTabWidth()
                var filteredReader = reader

                /* Setup the filters on the reader */
                for (codeReaderFilter in configuration.getCodeReaderFilters()) {
                    filteredReader = Filter(filteredReader, codeReaderFilter, configuration)
                }
                filteredReader.use { usedReader -> buffer = read(usedReader) }
            }
        } catch (e: IOException) {
            throw ChannelException(e.message, e)
        }
    }

    public constructor(code: String, configuration: CodeReaderConfiguration) : this(StringReader(code), configuration)

    private fun read(reader: Reader): CharArray {
        return reader.readText().toCharArray()
    }

    /**
     * Read and consume the next character
     *
     * @return the next character or -1 if the end of the stream is reached
     */
    public fun pop(): Int {
        if (bufferPosition >= buffer.size) {
            return -1
        }
        val character = buffer[bufferPosition].code
        bufferPosition++
        updateCursorPosition(character)
        if (recordingMode) {
            recordedCharacters.append(character.toChar())
        }
        lastChar = character
        return character
    }

    private fun updateCursorPosition(character: Int) {
        // see Java Language Specification : http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.4
        if (character == LF.code || character == CR.code && peek() != LF.code) {
            cursor.line++
            cursor.column = 0
        } else if (character == '\t'.code) {
            cursor.column += tabWidth
        } else {
            cursor.column++
        }
    }

    /**
     * Looks at the last consumed character
     *
     * @return the last character or -1 if the no character has been yet consumed
     */
    public fun lastChar(): Int {
        return lastChar
    }

    /**
     * Looks at the next character without consuming it
     *
     * @return the next character or -1 if the end of the stream has been reached
     */
    public fun peek(): Int {
        return intAt(0)
    }

    /**
     * @return the current line of the cursor
     */
    public fun getLinePosition(): Int {
        return cursor.line
    }

    /**
     * @return the current column of the cursor
     */
    public fun getColumnPosition(): Int {
        return cursor.column
    }

    /**
     * Overrides the current column position
     */
    public fun setColumnPosition(cp: Int): CodeBuffer {
        cursor.column = cp
        return this
    }

    /**
     * Overrides the current line position
     */
    public fun setLinePosition(lp: Int) {
        cursor.line = lp
    }

    public fun startRecording() {
        recordingMode = true
    }

    public fun stopRecording(): CharSequence {
        recordingMode = false
        val result: CharSequence = recordedCharacters
        recordedCharacters = StringBuilder()
        return result
    }

    /**
     * Returns the character at the specified index after the cursor without consuming it
     *
     * @param index
     * the relative index of the character to be returned
     * @return the desired character
     * @see java.lang.CharSequence.charAt
     */
    override fun get(index: Int): Char {
        return intAt(index).toChar()
    }

    public fun intAt(index: Int): Int {
        return if (bufferPosition + index >= buffer.size) {
            -1
        } else buffer[bufferPosition + index].code
    }

    /**
     * Returns the relative length of the string (i.e. excluding the popped chars)
     */
    override val length: Int
        get() {
            return buffer.size - bufferPosition
        }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("CodeReader(")
        result.append("line:").append(cursor.line)
        result.append("|column:").append(cursor.column)
        result.append("|cursor value:'").append(peek().toChar()).append("'")
        result.append(")")
        return result.toString()
    }

    public class Cursor : Cloneable {
        public var line: Int = 1
        public var column: Int = 0

        public override fun clone(): Cursor {
            val clone = try {
                super.clone() as Cursor
            } catch (e: CloneNotSupportedException) {
                throw RuntimeException(e)
            }
            clone.column = column
            clone.line = line
            return clone
        }
    }

    /**
     * Bridge class between CodeBuffer and CodeReaderFilter
     */
    internal class Filter(
        `in`: Reader,
        private val codeReaderFilter: CodeReaderFilter<*>,
        configuration: CodeReaderConfiguration
    ) : FilterReader(`in`) {
        init {
            codeReaderFilter.configuration = configuration.cloneWithoutCodeReaderFilters()
            codeReaderFilter.reader = `in`
        }

        @Throws(IOException::class)
        override fun read(): Int {
            throw UnsupportedOperationException()
        }

        @Throws(IOException::class)
        override fun read(cbuf: CharArray, off: Int, len: Int): Int {
            val read = codeReaderFilter.read(cbuf, off, len)
            return if (read == 0) -1 else read
        }

        @Throws(IOException::class)
        override fun skip(n: Long): Long {
            throw UnsupportedOperationException()
        }
    }

    private companion object {
        private const val LF = '\n'
        private const val CR = '\r'
    }
}
