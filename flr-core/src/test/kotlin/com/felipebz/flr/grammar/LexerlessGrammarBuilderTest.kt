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
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import com.felipebz.flr.internal.grammar.MutableGrammar
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.*
import java.util.regex.PatternSyntaxException

class LexerlessGrammarBuilderTest {
    @Test
    fun should_create_expressions() {
        val b = LexerlessGrammarBuilder.create()
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        val e3 = mock<ParsingExpression>()
        assertThat(b.convertToExpression(e1)).isSameAs(e1)
        assertThat(b.convertToExpression("")).isInstanceOf(StringExpression::class.java)
        assertThat(b.convertToExpression('c')).isInstanceOf(StringExpression::class.java)
        val ruleKey = mock<GrammarRuleKey>()
        assertThat(b.convertToExpression(ruleKey)).isInstanceOf(MutableParsingRule::class.java)
        assertThat(b.convertToExpression(ruleKey)).isSameAs(b.convertToExpression(ruleKey))
        assertThat(b.sequence(e1, e2)).isInstanceOf(SequenceExpression::class.java)
        assertThat(b.sequence(e1, e2, e3)).isInstanceOf(SequenceExpression::class.java)
        assertThat(b.firstOf(e1, e2)).isInstanceOf(FirstOfExpression::class.java)
        assertThat(b.firstOf(e1, e2, e3)).isInstanceOf(FirstOfExpression::class.java)
        assertThat(b.optional(e1)).isInstanceOf(OptionalExpression::class.java)
        assertThat(b.optional(e1, e2)).isInstanceOf(OptionalExpression::class.java)
        assertThat(b.oneOrMore(e1)).isInstanceOf(OneOrMoreExpression::class.java)
        assertThat(b.oneOrMore(e1, e2)).isInstanceOf(OneOrMoreExpression::class.java)
        assertThat(b.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression::class.java)
        assertThat(b.zeroOrMore(e1, e2)).isInstanceOf(ZeroOrMoreExpression::class.java)
        assertThat(b.next(e1)).isInstanceOf(NextExpression::class.java)
        assertThat(b.next(e1, e2)).isInstanceOf(NextExpression::class.java)
        assertThat(b.nextNot(e1)).isInstanceOf(NextNotExpression::class.java)
        assertThat(b.nextNot(e1, e2)).isInstanceOf(NextNotExpression::class.java)
        assertThat(b.nothing()).`as`("singleton").isSameAs(NothingExpression.INSTANCE)
        assertThat(b.regexp("")).isInstanceOf(PatternExpression::class.java)
        assertThat(b.endOfInput()).`as`("singleton").isSameAs(EndOfInputExpression.INSTANCE)
    }

    @Test
    fun test_token() {
        val tokenType = mock<TokenType>()
        val e = mock<ParsingExpression>()
        val result = LexerlessGrammarBuilder.create().token(tokenType, e)
        assertThat(result).isInstanceOf(TokenExpression::class.java)
        assertThat((result as TokenExpression).getTokenType()).isSameAs(tokenType)
    }

    @Test
    fun test_commentTrivia() {
        val e = mock<ParsingExpression>()
        val result = LexerlessGrammarBuilder.create().commentTrivia(e)
        assertThat(result).isInstanceOf(TriviaExpression::class.java)
        assertThat((result as TriviaExpression).getTriviaKind()).isEqualTo(TriviaKind.COMMENT)
    }

    @Test
    fun test_skippedTrivia() {
        val e = mock<ParsingExpression>()
        val result = LexerlessGrammarBuilder.create().skippedTrivia(e)
        assertThat(result).isInstanceOf(TriviaExpression::class.java)
        assertThat((result as TriviaExpression).getTriviaKind()).isEqualTo(TriviaKind.SKIPPED_TEXT)
    }

    @Test
    fun should_set_root_rule() {
        val b = LexerlessGrammarBuilder.create()
        val ruleKey = mock<GrammarRuleKey>()
        b.rule(ruleKey).`is`(b.nothing())
        b.setRootRule(ruleKey)
        val grammar = b.build() as MutableGrammar
        assertThat((grammar.rootRule as CompilableGrammarRule).ruleKey).isSameAs(ruleKey)
    }

    @Test
    fun test_undefined_root_rule() {
        val b = LexerlessGrammarBuilder.create()
        val ruleKey = mock<GrammarRuleKey>()
        b.setRootRule(ruleKey)
        assertThrows<GrammarException>("The rule '$ruleKey' hasn't been defined.") {
            b.build()
        }
    }

    @Test
    fun test_undefined_rule() {
        val b = LexerlessGrammarBuilder.create()
        val ruleKey = mock<GrammarRuleKey>()
        b.rule(ruleKey)
        assertThrows<GrammarException>("The rule '$ruleKey' hasn't been defined.") {
            b.build()
        }
    }

    @Test
    fun test_used_undefined_rule() {
        val b = LexerlessGrammarBuilder.create()
        val ruleKey1 = mock<GrammarRuleKey>()
        val ruleKey2 = mock<GrammarRuleKey>()
        b.rule(ruleKey1).`is`(ruleKey2)
        assertThrows<GrammarException>("The rule '$ruleKey2' hasn't been defined.") {
            b.build()
        }
    }

    @Test
    fun test_wrong_regexp() {
        val b = LexerlessGrammarBuilder.create()
        assertThrows<PatternSyntaxException> {
            b.regexp("[")
        }
    }

    @Test
    fun test_incorrect_type_of_parsing_expression() {
        assertThrows<IllegalArgumentException>("Incorrect type of parsing expression: class java.lang.Object") {
            LexerlessGrammarBuilder.create().convertToExpression(Any())
        }
    }

    @Test
    fun should_fail_to_redefine() {
        val b = LexerlessGrammarBuilder.create()
        val ruleKey = mock<GrammarRuleKey>()
        b.rule(ruleKey).`is`("foo")
        assertThrows<GrammarException>("The rule '$ruleKey' has already been defined somewhere in the grammar.") {
            b.rule(ruleKey).`is`("foo")
        }
    }

    @Test
    fun should_fail_to_redefine2() {
        val b = LexerlessGrammarBuilder.create()
        val ruleKey = mock<GrammarRuleKey>()
        b.rule(ruleKey).`is`("foo", "bar")
        assertThrows<GrammarException>("The rule '$ruleKey' has already been defined somewhere in the grammar.") {
            b.rule(ruleKey).`is`("foo", "bar")
        }
    }
}