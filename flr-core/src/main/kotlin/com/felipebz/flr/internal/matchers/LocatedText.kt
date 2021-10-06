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
package com.felipebz.flr.internal.matchers

import java.io.File
import java.net.URI
import java.util.*

public class LocatedText(private val file: File?, private val chars: CharArray) : CharSequence {
    private val uri: URI? = file?.toURI()

    /**
     * Indices of lines.
     * Number of elements equal to number of line terminators.
     */
    private val lines: IntArray = computeLines(chars)
    override val length: Int
        get() {
            return chars.size
        }

    public fun toChars(): CharArray {
        val chars = CharArray(length)
        System.arraycopy(this.chars, 0, chars, 0, chars.size)
        return chars
    }

    override fun get(index: Int): Char {
        return chars[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return String(toChars())
    }

    public fun getLocation(index: Int): TextLocation {
        if (index < 0 || index > length) {
            throw IndexOutOfBoundsException()
        }
        val line = getLineNumber(index)
        val column = index - getLineStart(line) + 1
        return TextLocation(file, uri, line, column)
    }

    private fun getLineNumber(index: Int): Int {
        val i = Arrays.binarySearch(lines, index)
        return if (i >= 0) i + 2 else -i
    }

    private fun getLineStart(line: Int): Int {
        return if (line == 1) 0 else lines[line - 2]
    }

    public companion object {
        private val EMPTY_INT_ARRAY = IntArray(0)
        private fun computeLines(chars: CharArray): IntArray {
            val newlines = mutableListOf<Int>()
            var i = 0
            while (i < chars.size) {
                if (isEndOfLine(chars, i)) {
                    newlines.add(i + 1)
                }
                i++
            }
            if (newlines.isEmpty()) {
                return EMPTY_INT_ARRAY
            }
            val lines = IntArray(newlines.size)
            i = 0
            while (i < newlines.size) {
                lines[i] = newlines[i]
                i++
            }
            return lines
        }

        /**
         * A line is considered to be terminated by any one of
         * a line feed (`'\n'`), a carriage return (`'\r'`),
         * or a carriage return followed immediately by a line feed (`"\r\n"`).
         */
        private fun isEndOfLine(buffer: CharArray, i: Int): Boolean {
            return buffer[i] == '\n' ||
                    buffer[i] == '\r' && (i + 1 < buffer.size && buffer[i + 1] != '\n' || i + 1 == buffer.size)
        }
    }

}