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
package com.felipebz.flr.api.typed

import java.io.File
import java.util.*

/**
 * @since 1.21
 */
public class Input constructor(private val inputChars: CharArray) {
    private val newLineIndexes: IntArray

    init {
        val newLineIndexesBuilder = mutableListOf<Int>()
        for (i in inputChars.indices) {
            if (isNewLine(inputChars, i)) {
                newLineIndexesBuilder.add(i + 1)
            }
        }
        newLineIndexes = newLineIndexesBuilder.toIntArray()
    }

    public fun input(): CharArray {
        return inputChars
    }

    public fun substring(from: Int, to: Int): String {
        val sb = StringBuilder()
        for (i in from until to) {
            sb.append(inputChars[i])
        }
        return sb.toString()
    }

    public fun lineAndColumnAt(index: Int): IntArray {
        val result = IntArray(2)
        result[0] = lineAt(index)
        result[1] = index - lineStartIndex(result[0]) + 1
        return result
    }

    private fun lineAt(index: Int): Int {
        val i = Arrays.binarySearch(newLineIndexes, index)
        return if (i >= 0) i + 2 else -i
    }

    private fun lineStartIndex(line: Int): Int {
        return if (line == 1) 0 else newLineIndexes[line - 2]
    }

    public companion object {
        private val FAKE_URI = File("tests://unittests").toURI()

        /**
         * New lines are: \n, \r\n (in which case true is returned for the \n) and \r alone.
         */
        private fun isNewLine(input: CharArray, i: Int): Boolean {
            return input[i] == '\n' ||
                    input[i] == '\r' && (i + 1 == input.size || input[i + 1] != '\n')
        }
    }
}