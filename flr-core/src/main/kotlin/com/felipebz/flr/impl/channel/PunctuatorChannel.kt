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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import java.util.*
import kotlin.math.max

public class PunctuatorChannel(vararg punctuators: TokenType) : Channel<LexerOutput> {
    private val lookahead: Int
    private val sortedPunctuators = punctuators
    private val sortedPunctuatorsChars: Array<CharArray>

    init {
        Arrays.sort(sortedPunctuators, PunctuatorComparator())
        var maxLength = 0

        sortedPunctuatorsChars = sortedPunctuators.map {
            val array = it.value.toCharArray()
            maxLength = max(maxLength, array.size)
            array
        }.toTypedArray()

        lookahead = maxLength
    }

    private class PunctuatorComparator : Comparator<TokenType> {
        override fun compare(a: TokenType, b: TokenType): Int {
            if (a.value.length == b.value.length) {
                return 0
            }
            return if (a.value.length > b.value.length) -1 else 1
        }
    }

    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        val next = code.peek(lookahead)
        for (i in sortedPunctuators.indices) {
            if (arraysEquals(next, sortedPunctuatorsChars[i])) {
                val token = Token.builder()
                    .setType(sortedPunctuators[i])
                    .setValueAndOriginalValue(sortedPunctuators[i].value)
                    .setLine(code.getLinePosition())
                    .setColumn(code.getColumnPosition())
                    .build()
                output.addToken(token)

                /* Advance the CodeReader stream by the length of the punctuator */
                for (j in 0 until sortedPunctuatorsChars[i].size) {
                    code.pop()
                }
                return true
            }
        }
        return false
    }

    public companion object {
        /**
         * Expected that length of second array can be less than length of first.
         */
        private fun arraysEquals(a: CharArray, a2: CharArray): Boolean {
            val length = a2.size
            for (i in 0 until length) {
                if (a[i] != a2[i]) {
                    return false
                }
            }
            return true
        }
    }
}
