//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.grammar.LexerlessGrammarBuilder.Companion.create

/**
 * This class demonstrates how to use [LexerlessGrammarBuilder] to define grammar for simple arithmetic expressions.
 */
public enum class ExpressionGrammar : GrammarRuleKey {
    WHITESPACE, PLUS, MINUS, DIV, MUL, NUMBER, VARIABLE, LPAR, RPAR, EXPRESSION, ADDITIVE_EXPRESSION, MULTIPLICATIVE_EXPRESSION, PRIMARY, PARENS;

    public companion object {
        @JvmStatic
        public fun createGrammarBuilder(): LexerlessGrammarBuilder {
            val b = create()
            b.rule(WHITESPACE).`is`(b.commentTrivia(b.regexp("\\s*+"))).skip()
            b.rule(PLUS).`is`('+', WHITESPACE)
            b.rule(MINUS).`is`('-', WHITESPACE)
            b.rule(DIV).`is`('/', WHITESPACE)
            b.rule(MUL).`is`('*', WHITESPACE)
            b.rule(NUMBER).`is`(b.regexp("[0-9]++"), WHITESPACE)
            b.rule(VARIABLE).`is`(b.regexp("\\p{javaJavaIdentifierStart}++\\p{javaJavaIdentifierPart}*+"), WHITESPACE)
            b.rule(LPAR).`is`('(', WHITESPACE)
            b.rule(RPAR).`is`(')', WHITESPACE)
            b.rule(EXPRESSION).`is`(WHITESPACE, ADDITIVE_EXPRESSION, b.endOfInput())
            b.rule(ADDITIVE_EXPRESSION)
                .`is`(MULTIPLICATIVE_EXPRESSION, b.zeroOrMore(b.firstOf(PLUS, MINUS), MULTIPLICATIVE_EXPRESSION))
            b.rule(MULTIPLICATIVE_EXPRESSION).`is`(PRIMARY, b.zeroOrMore(b.firstOf(DIV, MUL), PRIMARY)).skipIfOneChild()
            b.rule(PRIMARY).`is`(b.firstOf(NUMBER, PARENS, VARIABLE)).skipIfOneChild()
            b.rule(PARENS).`is`(LPAR, ADDITIVE_EXPRESSION, RPAR)
            b.setRootRule(EXPRESSION)
            return b
        }
    }
}
