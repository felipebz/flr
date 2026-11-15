//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

public class Trivia private constructor(
    private val kind: TriviaKind,
    vararg tokens: Token
) {
    public enum class TriviaKind {
        COMMENT, SKIPPED_TEXT
    }

    public val tokens: List<Token> = listOf(*tokens)

    public val token: Token
        get() = tokens[0]
    public val isComment: Boolean
        get() = kind == TriviaKind.COMMENT
    public val isSkippedText: Boolean
        get() = kind == TriviaKind.SKIPPED_TEXT

    init {
        require(this.tokens.isNotEmpty()) { "the trivia must have at least one associated token to be able to call getToken()" }
    }

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

    public companion object {
        public fun createComment(commentToken: Token): Trivia {
            return Trivia(TriviaKind.COMMENT, commentToken)
        }

        public fun createSkippedText(tokens: List<Token>): Trivia {
            return createSkippedText(*tokens.toTypedArray())
        }

        public fun createSkippedText(vararg tokens: Token): Trivia {
            return Trivia(TriviaKind.SKIPPED_TEXT, *tokens)
        }
    }
}
