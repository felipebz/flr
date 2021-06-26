/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package com.sonar.sslr.test.lexer

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import org.sonar.sslr.channel.CodeReader
import java.util.regex.Pattern

object TokenUtils {
    fun merge(tokens: MutableList<Token>): String {
        var tokens = tokens
        tokens = removeLastTokenIfEof(tokens)
        val result = StringBuilder()
        for (i in tokens.indices) {
            val token = tokens[i]
            result.append(token.value)
            if (i < tokens.size - 1) {
                result.append(" ")
            }
        }
        return result.toString()
    }

    private fun removeLastTokenIfEof(tokens: MutableList<Token>): MutableList<Token> {
        if (tokens.isNotEmpty()) {
            val lastToken = tokens[tokens.size - 1]
            if ("EOF" == lastToken.value) {
                return tokens.subList(0, tokens.size - 1)
            }
        }
        return tokens
    }

    fun lex(sourceCode: String): MutableList<Token?> {
        val tokens: MutableList<Token?> = ArrayList()
        val reader = CodeReader(sourceCode)
        val matcher = Pattern.compile("[a-zA-Z_0-9\\+\\-\\*/]+").matcher("")
        while (reader.peek() != -1) {
            val nextStringToken = StringBuilder()
            var token: Token
            val linePosition = reader.getLinePosition()
            val columnPosition = reader.getColumnPosition()
            token = if (reader.popTo(matcher, nextStringToken) != -1) {
                if ("EOF" == nextStringToken.toString()) {
                    MockHelper.mockTokenBuilder(GenericTokenType.EOF, nextStringToken.toString()).setLine(linePosition)
                        .setColumn(columnPosition).build()
                } else {
                    MockHelper.mockTokenBuilder(GenericTokenType.IDENTIFIER, nextStringToken.toString())
                        .setLine(linePosition)
                        .setColumn(columnPosition).build()
                }
            } else if (Character.isWhitespace(reader.peek())) {
                reader.pop()
                continue
            } else {
                MockHelper.mockTokenBuilder(
                    GenericTokenType.IDENTIFIER,
                    reader.pop().toChar().toString()
                ).setLine(linePosition).setColumn(columnPosition).build()
            }
            tokens.add(token)
        }
        return tokens
    }
}