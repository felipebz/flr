//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.matchers

import java.util.*
import kotlin.math.min

public class ImmutableInputBuffer(private val buffer: CharArray) : InputBuffer {
    /**
     * Indices of lines in buffer.
     */
    private val lines: IntArray

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
        lines = newlines.toIntArray()
    }

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
}
