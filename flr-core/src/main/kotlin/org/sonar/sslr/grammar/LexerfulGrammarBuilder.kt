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
package org.sonar.sslr.grammar

import com.sonar.sslr.api.Grammar
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.grammar.MutableGrammar
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.internal.vm.lexerful.*
import java.util.*

/**
 * A builder for creating [Parsing Expression Grammars](http://en.wikipedia.org/wiki/Parsing_expression_grammar) for lexerful parsing.
 * [com.sonar.sslr.impl.Lexer] is required for parsers of such grammars.
 *
 *
 * Objects of following types can be used as an atomic parsing expressions:
 *
 *  * GrammarRuleKey
 *  * TokenType
 *  * String
 *
 *
 * @since 1.18
 * @see LexerlessGrammarBuilder
 */
public class LexerfulGrammarBuilder private constructor() : GrammarBuilder() {
    private val definitions: MutableMap<GrammarRuleKey, RuleDefinition> = HashMap()
    private var rootRuleKey: GrammarRuleKey? = null

    /**
     * {@inheritDoc}
     */
    override fun rule(ruleKey: GrammarRuleKey): GrammarRuleBuilder {
        var rule = definitions[ruleKey]
        if (rule == null) {
            rule = RuleDefinition(ruleKey)
            definitions[ruleKey] = rule
        }
        return RuleBuilder(this, rule)
    }

    /**
     * {@inheritDoc}
     */
    override fun setRootRule(ruleKey: GrammarRuleKey) {
        rule(ruleKey)
        rootRuleKey = ruleKey
    }

    /**
     * Constructs grammar.
     *
     * @throws GrammarException if some of rules were used, but not defined
     * @return grammar
     * @see .buildWithMemoizationOfMatchesForAllRules
     */
    public fun build(): Grammar {
        for (rule in definitions.values) {
            if (rule.expression == null) {
                throw GrammarException("The rule '" + rule.ruleKey + "' hasn't been defined.")
            }
        }
        return MutableGrammar(definitions, rootRuleKey)
    }

    /**
     * Constructs grammar with memoization of matches for all rules.
     *
     * @throws GrammarException if some of rules were used, but not defined
     * @return grammar
     * @see .build
     */
    public fun buildWithMemoizationOfMatchesForAllRules(): Grammar {
        for (rule in definitions.values) {
            rule.enableMemoization()
        }
        return build()
    }

    /**
     * Creates parsing expression - "adjacent".
     * During execution of this expression parser will execute sub-expression only if there is no space between next and previous tokens.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun adjacent(e: Any): Any {
        return SequenceExpression(AdjacentExpression.INSTANCE, convertToExpression(e))
    }

    /**
     * Creates parsing expression - "any token but not".
     * Equivalent of expression `sequence(nextNot(e), anyToken())`
     * Do not overuse this method.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun anyTokenButNot(e: Any): Any {
        return SequenceExpression(NextNotExpression(convertToExpression(e)), AnyTokenExpression.INSTANCE)
    }

    /**
     * Creates parsing expression - "is one of them".
     * During execution of this expression parser will consume following token only if its type belongs to the provided list.
     * Equivalent of expression `firstOf(t1, rest)`.
     * Do not overuse this method.
     *
     * @param t1  first type of token
     * @param rest  rest of types
     */
    public fun isOneOfThem(t1: TokenType, vararg rest: TokenType): Any {
        val types = arrayOfNulls<TokenType>(1 + rest.size)
        types[0] = t1
        System.arraycopy(rest, 0, types, 1, rest.size)
        return TokenTypesExpression(*types.requireNoNulls())
    }

    /**
     * Creates parsing expression - "bridge".
     * Equivalent of:
     * <pre>
     * rule(bridge).is(
     * from,
     * zeroOrMore(firstOf(
     * sequence(nextNot(firstOf(from, to)), anyToken()),
     * bridge
     * )),
     * to
     * ).skip()
    </pre> *
     * Do not overuse this expression.
     */
    public fun bridge(from: TokenType, to: TokenType): Any {
        return TokensBridgeExpression(from, to)
    }

    /**
     * Creates parsing expression - "any token".
     * During execution of this expression parser will unconditionally consume following token.
     * This expression fails, if end of input reached.
     */
    public fun anyToken(): Any {
        return AnyTokenExpression.INSTANCE
    }

    /**
     * Creates parsing expression - "till new line".
     * During execution of this expression parser will consume all following tokens, which are on the current line.
     * This expression always succeeds.
     * Do not overuse this expression.
     */
    public fun tillNewLine(): Any {
        return TillNewLineExpression.INSTANCE
    }

    /**
     * Creates parsing expression - "till".
     * Equivalent of expression `sequence(zeroOrMore(nextNot(e), anyToken()), e)`.
     * Do not overuse this method.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun till(e: Any): Any {
        // TODO repeated expression
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

    /**
     * Creates parsing expression - "exclusive till".
     * Equivalent of expression `zeroOrMore(nextNot(e), anyToken())`.
     * Do not overuse this method.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    public fun exclusiveTill(e: Any): Any {
        return ZeroOrMoreExpression(
            SequenceExpression(
                NextNotExpression(convertToExpression(e)),
                AnyTokenExpression.INSTANCE
            )
        )
    }

    /**
     * Creates parsing expression - "exclusive till".
     * Equivalent of expression `zeroOrMore(nextNot(firstOf(e, rest)), anyToken())`.
     * Do not overuse this method.
     *
     * @param e1  first sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    public fun exclusiveTill(e1: Any, vararg rest: Any): Any {
        return exclusiveTill(FirstOfExpression(*convertToExpressions(e1, rest)))
    }

    override fun convertToExpression(e: Any): ParsingExpression {
        return when (e) {
            is ParsingExpression -> {
                e
            }
            is GrammarRuleKey -> {
                rule(e)
                checkNotNull(definitions[e])
            }
            is TokenType -> {
                TokenTypeExpression(e)
            }
            is String -> {
                TokenValueExpression(e)
            }
            is Class<*> -> {
                TokenTypeClassExpression(e)
            }
            else -> {
                throw IllegalArgumentException("Incorrect type of parsing expression: " + e.javaClass.toString())
            }
        }
    }

    public companion object {
        @JvmStatic
        public fun create(): LexerfulGrammarBuilder {
            return LexerfulGrammarBuilder()
        }
    }
}