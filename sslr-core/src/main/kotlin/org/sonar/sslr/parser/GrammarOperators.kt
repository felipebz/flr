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
package org.sonar.sslr.parser

import com.sonar.sslr.api.TokenType
import com.sonar.sslr.api.Trivia.TriviaKind
import org.sonar.sslr.internal.vm.*
import java.util.*

/**
 * @since 1.16
 */
@Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder} instead.")
public object GrammarOperators {

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#sequence(Object, Object)} instead.")
    public fun sequence(vararg e: Any): Any {
        return convertToSingleExpression(*e)
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#firstOf(Object, Object)} instead.")
    public fun firstOf(vararg e: Any): Any {
        Objects.requireNonNull(e)
        return if (e.size == 1) {
            convertToExpression(e[0])
        } else FirstOfExpression(*convertToExpressions(*e))
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#optional(Object)} instead.")
    public fun optional(vararg e: Any): Any {
        return OptionalExpression(convertToSingleExpression(*e))
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#oneOrMore(Object)} instead.")
    public fun oneOrMore(vararg e: Any): Any {
        return OneOrMoreExpression(convertToSingleExpression(*e))
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#zeroOrMore(Object)} instead.")
    public fun zeroOrMore(vararg e: Any): Any {
        return ZeroOrMoreExpression(convertToSingleExpression(*e))
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#next(Object)} instead.")
    public fun next(vararg e: Any): Any {
        return NextExpression(convertToSingleExpression(*e))
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#nextNot(Object)} instead.")
    public fun nextNot(vararg e: Any): Any {
        return NextNotExpression(convertToSingleExpression(*e))
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#regexp(String)} instead.")
    public fun regexp(regexp: String): Any {
        return PatternExpression(regexp)
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#endOfInput()} instead.")
    public fun endOfInput(): Any {
        return EndOfInputExpression.INSTANCE
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#nothing()} instead.")
    public fun nothing(): Any {
        return NothingExpression.INSTANCE
    }

    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#token(TokenType, Object)} instead.")
    public fun token(tokenType: TokenType, e: Any): Any {
        return TokenExpression(tokenType, convertToExpression(e))
    }

    /**
     * @since 1.17
     */
    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#commentTrivia(Object)} instead.")
    public fun commentTrivia(e: Any): Any {
        return TriviaExpression(TriviaKind.COMMENT, convertToExpression(e))
    }

    /**
     * @since 1.17
     */
    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerlessGrammarBuilder#skippedTrivia(Object)} instead.")
    public fun skippedTrivia(e: Any): Any {
        return TriviaExpression(TriviaKind.SKIPPED_TEXT, convertToExpression(e))
    }

    private fun convertToSingleExpression(vararg elements: Any): ParsingExpression {
        return if (elements.size == 1) {
            convertToExpression(elements[0])
        } else SequenceExpression(*convertToExpressions(*elements))
    }

    private fun convertToExpressions(vararg elements: Any): Array<ParsingExpression> {
        require(elements.isNotEmpty())
        val matchers = arrayOfNulls<ParsingExpression>(elements.size)
        for (i in matchers.indices) {
            matchers[i] = convertToExpression(elements[i])
        }
        return matchers.requireNoNulls()
    }

    private fun convertToExpression(e: Any): ParsingExpression {
        Objects.requireNonNull(e)
        return when (e) {
            is ParsingExpression -> {
                e
            }
            is String -> {
                StringExpression(e)
            }
            is Char -> {
                StringExpression(e.toString())
            }
            else -> {
                throw IllegalArgumentException("Incorrect type of parsing expression: " + e.javaClass.toString())
            }
        }
    }
}