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
package com.sonar.sslr.api

import java.util.*

class Trivia private constructor(
    private val kind: TriviaKind,
    vararg tokens: Token
) {
    enum class TriviaKind {
        COMMENT, SKIPPED_TEXT
    }

    val tokens: List<Token> = listOf(*tokens)

    val token: Token
        get() = tokens[0]
    val isComment: Boolean
        get() = kind == TriviaKind.COMMENT
    val isSkippedText: Boolean
        get() = kind == TriviaKind.SKIPPED_TEXT

    override fun toString(): String {
        return when {
            tokens.isEmpty() -> {
                "TRIVIA kind=$kind"
            }
            tokens.size == 1 -> {
                val token = tokens[0]
                "TRIVIA kind=" + kind + " line=" + token.line + " type=" + token.type + " value=" + token.originalValue
            }
            else -> {
                val sb = StringBuilder()
                for (token in tokens) {
                    sb.append(token.originalValue)
                    sb.append(' ')
                }
                "TRIVIA kind=$kind value = $sb"
            }
        }
    }

    companion object {
        fun createComment(commentToken: Token): Trivia {
            return Trivia(TriviaKind.COMMENT, commentToken)
        }

        fun createSkippedText(tokens: List<Token>): Trivia {
            Objects.requireNonNull(tokens, "tokens cannot be null")
            return createSkippedText(*tokens.toTypedArray())
        }

        fun createSkippedText(vararg tokens: Token): Trivia {
            return Trivia(TriviaKind.SKIPPED_TEXT, *tokens)
        }
    }

    init {
        require(this.tokens.isNotEmpty()) { "the trivia must have at least one associated token to be able to call getToken()" }
    }
}