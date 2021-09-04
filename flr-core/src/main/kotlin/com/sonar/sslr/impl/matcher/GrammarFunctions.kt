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
package com.sonar.sslr.impl.matcher

import com.sonar.sslr.api.Grammar
import com.sonar.sslr.api.TokenType
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.internal.vm.lexerful.*

@Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder} instead.")
public object GrammarFunctions {
    /**
     * @since 1.14
     */
    @JvmStatic
    @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#buildWithMemoizationOfMatchesForAllRules()} instead.")
    public fun enableMemoizationOfMatchesForAllRules(grammar: Grammar) {
        for (ruleField in Grammar.getAllRuleFields(grammar.javaClass)) {
            val ruleName = ruleField.name
            val rule = try {
                ruleField[grammar] as RuleDefinition
            } catch (e: IllegalAccessException) {
                throw IllegalStateException("Unable to enable memoization for rule '$ruleName'", e)
            }
            rule.enableMemoization()
        }
    }

    public fun convertToSingleExpression(e: Array<out Any>): ParsingExpression {
        checkSize(e)
        return if (e.size == 1) {
            convertToExpression(e[0])
        } else {
            SequenceExpression(*convertToExpressions(e))
        }
    }

    private fun convertToExpressions(e: Array<out Any>): Array<out ParsingExpression> {
        checkSize(e)
        val matchers = arrayOfNulls<ParsingExpression>(e.size)
        for (i in matchers.indices) {
            matchers[i] = convertToExpression(e[i])
        }
        return matchers.requireNoNulls()
    }

    private fun convertToExpression(e: Any): ParsingExpression {
        return when (e) {
            is String -> {
                TokenValueExpression(e)
            }
            is TokenType -> {
                TokenTypeExpression(e)
            }
            is RuleDefinition -> {
                e
            }
            is Class<*> -> {
                TokenTypeClassExpression(e)
            }
            is ParsingExpression -> {
                e
            }
            else -> {
                throw IllegalArgumentException("The matcher object can't be anything else than a Rule, Matcher, String, TokenType or Class. Object = $e")
            }
        }
    }

    private fun checkSize(e: Array<out Any>) {
        require(e.isNotEmpty()) { "You must define at least one matcher." }
    }

    public object Standard {

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#zeroOrMore(Object)} instead.")
        public fun o2n(vararg e: Any): Matcher {
            return ZeroOrMoreExpression(convertToSingleExpression(e))
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#oneOrMore(Object)} instead.")
        public fun one2n(vararg e: Any): Matcher {
            return OneOrMoreExpression(convertToSingleExpression(e))
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#optional(Object)} instead.")
        public fun opt(vararg e: Any): Matcher {
            return OptionalExpression(convertToSingleExpression(e))
        }

        @JvmStatic
        @Deprecated("in 1.16, use {@link GrammarFunctions.Standard#firstOf(Object...)} instead")
        public fun or(vararg e: Any): Matcher {
            return firstOf(*e)
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#firstOf(Object, Object)} instead.")
        public fun firstOf(vararg e: Any): Matcher {
            checkSize(e)
            return if (e.size == 1) {
                convertToExpression(e[0])
            } else {
                FirstOfExpression(*convertToExpressions(e))
            }
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#sequence(Object, Object)} instead.")
        public fun and(vararg e: Any): Matcher {
            return convertToSingleExpression(e)
        }
    }

    public object Predicate {

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#nextNot(Object)} instead.")
        public fun not(e: Any): Matcher {
            return NextNotExpression(convertToExpression(e))
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#next(Object)} instead.")
        public fun next(vararg e: Any): Matcher {
            return NextExpression(convertToSingleExpression(e))
        }
    }

    public object Advanced {

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#adjacent(Object)} instead.")
        public fun adjacent(e: Any): Matcher {
            return SequenceExpression(AdjacentExpression.INSTANCE, convertToExpression(e))
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#anyTokenButNot(Object)} instead.")
        public fun anyTokenButNot(e: Any): Matcher {
            return SequenceExpression(NextNotExpression(convertToExpression(e)), AnyTokenExpression.INSTANCE)
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#isOneOfThem(TokenType, TokenType...)} instead.")
        public fun isOneOfThem(vararg types: TokenType): Matcher {
            checkSize(types)
            return TokenTypesExpression(*types)
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#bridge(TokenType, TokenType)} instead.")
        public fun bridge(from: TokenType, to: TokenType): Matcher {
            return TokensBridgeExpression(from, to)
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#anyToken()} instead.")
        public fun isTrue(): Matcher {
            return AnyTokenExpression.INSTANCE
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#nothing()} instead.")
        public fun isFalse(): Matcher {
            return NothingExpression.INSTANCE
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#anyToken()} instead.")
        public fun anyToken(): Matcher {
            return AnyTokenExpression.INSTANCE
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#tillNewLine()} instead.")
        public fun tillNewLine(): Matcher {
            return TillNewLineExpression.INSTANCE
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#till(Object)} instead.")
        public fun till(e: Any): Matcher {
            val expression = convertToExpression(e)
            return SequenceExpression(
                ZeroOrMoreExpression(
                    SequenceExpression(
                        NextNotExpression(expression),
                        AnyTokenExpression.INSTANCE
                    )
                ),
                expression
            )
        }

        @JvmStatic
        @Deprecated("in 1.19, use {@link org.sonar.sslr.grammar.LexerfulGrammarBuilder#exclusiveTill(Object)} instead.")
        public fun exclusiveTill(vararg e: Any): Matcher {
            val expressions = convertToExpressions(e)
            val subExpression = if (expressions.size == 1) expressions[0] else FirstOfExpression(*expressions)
            return ZeroOrMoreExpression(
                SequenceExpression(
                    NextNotExpression(
                        subExpression
                    ),
                    AnyTokenExpression.INSTANCE
                )
            )
        }
    }
}