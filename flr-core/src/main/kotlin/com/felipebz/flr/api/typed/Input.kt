//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api.typed

import java.io.File
import java.util.*

public class Input(private val inputChars: CharArray) {
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
