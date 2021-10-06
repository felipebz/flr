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
package com.felipebz.flr.grammar

import com.felipebz.flr.api.TokenType
import com.felipebz.flr.api.Trivia.TriviaKind
import com.felipebz.flr.internal.grammar.MutableGrammar
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.*
import com.felipebz.flr.parser.LexerlessGrammar
import java.util.*

/**
 * A builder for creating [Parsing Expression Grammars](http://en.wikipedia.org/wiki/Parsing_expression_grammar) for lexerless parsing.
 *
 *
 * Objects of following types can be used as an atomic parsing expressions:
 *
 *  * GrammarRuleKey
 *  * String
 *  * Character
 *
 *
 * @since 1.18
 * @see LexerfulGrammarBuilder
 */
public class LexerlessGrammarBuilder private constructor() : GrammarBuilder() {
    private val definitions: MutableMap<GrammarRuleKey, MutableParsingRule> = HashMap()
    private var rootRuleKey: GrammarRuleKey? = null

    /**
     * {@inheritDoc}
     */
    override fun rule(ruleKey: GrammarRuleKey): GrammarRuleBuilder {
        var rule = definitions[ruleKey]
        if (rule == null) {
            rule = MutableParsingRule(ruleKey)
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
     */
    public fun build(): LexerlessGrammar {
        for (rule in definitions.values) {
            if (rule.expression == null) {
                throw GrammarException("The rule '" + rule.ruleKey + "' hasn't been defined.")
            }
        }
        return MutableGrammar(definitions, rootRuleKey)
    }

    /**
     * Creates parsing expression based on regular expression.
     *
     * @param regexp  regular expression
     * @throws java.util.regex.PatternSyntaxException if the expression's syntax is invalid
     */
    public fun regexp(regexp: String): Any {
        return PatternExpression(regexp)
    }

    /**
     * Creates parsing expression - "end of input".
     * This expression succeeds only if parser reached end of input.
     */
    public fun endOfInput(): Any {
        return EndOfInputExpression.INSTANCE
    }

    /**
     * Creates parsing expression - "token".
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun token(tokenType: TokenType, e: Any): Any {
        return TokenExpression(tokenType, convertToExpression(e))
    }

    /**
     * Creates parsing expression - "comment trivia".
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun commentTrivia(e: Any): Any {
        return TriviaExpression(TriviaKind.COMMENT, convertToExpression(e))
    }

    /**
     * Creates parsing expression - "skipped trivia".
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun skippedTrivia(e: Any): Any {
        return TriviaExpression(TriviaKind.SKIPPED_TEXT, convertToExpression(e))
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

    public companion object {
        @JvmStatic
        public fun create(): LexerlessGrammarBuilder {
            return LexerlessGrammarBuilder()
        }
    }
}