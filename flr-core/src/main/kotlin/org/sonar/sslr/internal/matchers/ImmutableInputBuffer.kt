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
package org.sonar.sslr.internal.matchers

import java.util.*
import kotlin.math.min

public class ImmutableInputBuffer(private val buffer: CharArray) : InputBuffer {
    /**
     * Indices of lines in buffer.
     */
    private val lines: IntArray
    override fun length(): Int {
        return buffer.size
    }

    override fun charAt(index: Int): Char {
        return buffer[index]
    }

    override fun extractLine(lineNumber: Int): String {
        val start = lines[lineNumber - 1]
        val end = lines[lineNumber]
        return String(buffer, start, end - start)
    }

    override fun getLineCount(): Int {
        return lines.size - 1
    }

    private fun getLineNumber(index: Int): Int {
        val i = Arrays.binarySearch(lines, index)
        return min(if (i >= 0) i + 1 else -(i + 1), getLineCount())
    }

    override fun getPosition(index: Int): InputBuffer.Position {
        val line = getLineNumber(index)
        val column = index - lines[line - 1] + 1
        return InputBuffer.Position(line, column)
    }

    public companion object {
        /**
         * A line is considered to be terminated by any one of
         * a line feed (`'\n'`), a carriage return (`'\r'`),
         * or a carriage return followed immediately by a line feed (`"\r\n"`).
         */
        private fun isEndOfLine(buffer: CharArray, i: Int): Boolean {
            return buffer[i] == TextUtils.LF ||
                    buffer[i] == TextUtils.CR && (i + 1 < buffer.size && buffer[i + 1] != TextUtils.LF || i + 1 == buffer.size)
        }
    }

    init {
        val newlines = mutableListOf<Int>()
        var i = 0
        newlines.add(0)
        while (i < buffer.size) {
            if (isEndOfLine(buffer, i)) {
                newlines.add(i + 1)
            }
            i++
        }
        newlines.add(i)
        lines = IntArray(newlines.size)
        i = 0
        while (i < newlines.size) {
            lines[i] = newlines[i]
            i++
        }
    }
}