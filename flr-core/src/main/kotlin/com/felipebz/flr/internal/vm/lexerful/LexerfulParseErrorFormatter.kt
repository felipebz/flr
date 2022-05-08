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
package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.Token
import kotlin.math.max
import kotlin.math.min

public class LexerfulParseErrorFormatter {
    public fun format(tokens: List<Token>, errorIndex: Int): String {
        val sb = StringBuilder()
        val errorPos = if (errorIndex < tokens.size) getTokenStart(
            tokens[errorIndex]
        ) else getTokenEnd(tokens[tokens.size - 1])
        sb.append("Parse error at line ").append(errorPos.line)
            .append(" column ").append(errorPos.column)
            .append(":\n\n")
        appendSnippet(sb, tokens, errorIndex, errorPos.line)
        return sb.toString()
    }

    private class Pos {
        var line = 0
        var column = 0
        override fun toString(): String {
            return "($line, $column)"
        }
    }

    public companion object {
        private val pattern = Regex("\\R")

        /**
         * Number of tokens in snippet before and after token with error.
         */
        private const val SNIPPET_SIZE = 30
        private fun getTokenStart(token: Token): Pos {
            val pos = Pos()
            pos.line = token.line
            pos.column = token.column
            return pos
        }

        private fun getTokenEnd(token: Token): Pos {
            val pos = Pos()
            pos.line = token.line
            pos.column = token.column
            val tokenLines = token.originalValue.split(pattern).toTypedArray()
            if (tokenLines.size == 1) {
                pos.column += tokenLines[0].length
            } else {
                pos.line += tokenLines.size - 1
                pos.column = tokenLines[tokenLines.size - 1].length
            }
            return pos
        }

        private fun appendSnippet(sb: StringBuilder, tokens: List<Token>, errorIndex: Int, errorLine: Int) {
            var tokens = tokens
            val startToken = max(errorIndex - SNIPPET_SIZE, 0)
            val endToken = min(errorIndex + SNIPPET_SIZE, tokens.size)
            tokens = tokens.subList(startToken, endToken)
            var line = tokens[0].line
            var column = tokens[0].column
            sb.append(formatLineNumber(line, errorLine))
            for (token in tokens) {
                while (line < token.line) {
                    line++
                    column = 0
                    sb.append('\n').append(formatLineNumber(line, errorLine))
                }
                while (column < token.column) {
                    sb.append(' ')
                    column++
                }
                val tokenLines = token.originalValue.split(pattern).toTypedArray()
                sb.append(tokenLines[0])
                column += tokenLines[0].length
                for (j in 1 until tokenLines.size) {
                    line++
                    sb.append('\n').append(formatLineNumber(line, errorLine)).append(
                        tokenLines[j]
                    )
                    column = tokenLines[j].length
                }
            }
            sb.append('\n')
        }

        private fun formatLineNumber(line: Int, errorLine: Int): String {
            return if (line == errorLine) String.format("%1$5s  ", "-->") else String.format("%1$5d: ", line)
        }
    }
}