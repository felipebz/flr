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
package org.sonar.sslr.grammar

import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.grammar.MutableGrammar
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.internal.vm.lexerful.*

class LexerfulGrammarBuilderTest {
    @Test
    fun should_create_expressions() {
        val b = LexerfulGrammarBuilder.create()
        val e1 = Mockito.mock(ParsingExpression::class.java)
        val e2 = Mockito.mock(ParsingExpression::class.java)
        val e3 = Mockito.mock(ParsingExpression::class.java)
        Assertions.assertThat(b.convertToExpression(e1)).isSameAs(e1)
        Assertions.assertThat(b.convertToExpression("")).isInstanceOf(TokenValueExpression::class.java)
        Assertions.assertThat(b.convertToExpression(Mockito.mock(TokenType::class.java))).isInstanceOf(
            TokenTypeExpression::class.java
        )
        Assertions.assertThat(b.convertToExpression(Any::class.java)).isInstanceOf(
            TokenTypeClassExpression::class.java
        )
        Assertions.assertThat(b.sequence(e1, e2)).isInstanceOf(SequenceExpression::class.java)
        Assertions.assertThat(b.sequence(e1, e2, e3)).isInstanceOf(SequenceExpression::class.java)
        Assertions.assertThat(b.firstOf(e1, e2)).isInstanceOf(FirstOfExpression::class.java)
        Assertions.assertThat(b.firstOf(e1, e2, e3)).isInstanceOf(FirstOfExpression::class.java)
        Assertions.assertThat(b.optional(e1)).isInstanceOf(OptionalExpression::class.java)
        Assertions.assertThat(b.optional(e1, e2)).isInstanceOf(OptionalExpression::class.java)
        Assertions.assertThat(b.oneOrMore(e1)).isInstanceOf(OneOrMoreExpression::class.java)
        Assertions.assertThat(b.oneOrMore(e1, e2)).isInstanceOf(OneOrMoreExpression::class.java)
        Assertions.assertThat(b.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression::class.java)
        Assertions.assertThat(b.zeroOrMore(e1, e2)).isInstanceOf(ZeroOrMoreExpression::class.java)
        Assertions.assertThat(b.next(e1)).isInstanceOf(NextExpression::class.java)
        Assertions.assertThat(b.next(e1, e2)).isInstanceOf(NextExpression::class.java)
        Assertions.assertThat(b.nextNot(e1)).isInstanceOf(NextNotExpression::class.java)
        Assertions.assertThat(b.nextNot(e1, e2)).isInstanceOf(NextNotExpression::class.java)
        Assertions.assertThat(b.nothing()).`as`("singleton").isSameAs(NothingExpression.INSTANCE)
        Assertions.assertThat(
            b.isOneOfThem(
                Mockito.mock(TokenType::class.java), Mockito.mock(
                    TokenType::class.java
                )
            )
        ).isInstanceOf(TokenTypesExpression::class.java)
        Assertions.assertThat(
            b.bridge(
                Mockito.mock(TokenType::class.java), Mockito.mock(
                    TokenType::class.java
                )
            )
        ).isInstanceOf(TokensBridgeExpression::class.java)
        Assertions.assertThat(b.adjacent(e1).toString()).isEqualTo("Sequence[Adjacent, $e1]")
        Assertions.assertThat(b.anyTokenButNot(e1).toString()).isEqualTo("Sequence[NextNot[$e1], AnyToken]")
        Assertions.assertThat(b.till(e1).toString())
            .isEqualTo("Sequence[ZeroOrMore[Sequence[NextNot[$e1], AnyToken]], $e1]")
        Assertions.assertThat(b.exclusiveTill(e1).toString()).isEqualTo("ZeroOrMore[Sequence[NextNot[$e1], AnyToken]]")
        Assertions.assertThat(b.exclusiveTill(e1, e2).toString())
            .isEqualTo("ZeroOrMore[Sequence[NextNot[FirstOf[$e1, $e2]], AnyToken]]")
        Assertions.assertThat(b.everything()).`as`("singleton").isSameAs(AnyTokenExpression.INSTANCE)
        Assertions.assertThat(b.anyToken()).`as`("singleton").isSameAs(AnyTokenExpression.INSTANCE)
        Assertions.assertThat(b.tillNewLine()).`as`("singleton").isSameAs(TillNewLineExpression.INSTANCE)
    }

    @Test
    fun should_set_root_rule() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        b.rule(ruleKey).`is`(b.nothing())
        b.setRootRule(ruleKey)
        val grammar = b.build() as MutableGrammar
        Assertions.assertThat((grammar.getRootRule() as CompilableGrammarRule).ruleKey).isSameAs(ruleKey)
    }

    @Test
    fun should_build_with_memoization() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        b.rule(ruleKey).`is`("foo")
        val grammar = b.buildWithMemoizationOfMatchesForAllRules()
        Assertions.assertThat((grammar.rule(ruleKey) as RuleDefinition).shouldMemoize()).isTrue()
    }

    @Test
    fun test_undefined_root_rule() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        b.setRootRule(ruleKey)
        assertThrows("The rule '$ruleKey' hasn't been defined.", GrammarException::class.java) {
            b.build()
        }
    }

    @Test
    fun test_undefined_rule() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        b.rule(ruleKey)
        assertThrows("The rule '$ruleKey' hasn't been defined.", GrammarException::class.java) {
            b.build()
        }
    }

    @Test
    fun test_used_undefined_rule() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey1 = Mockito.mock(GrammarRuleKey::class.java)
        val ruleKey2 = Mockito.mock(GrammarRuleKey::class.java)
        b.rule(ruleKey1).`is`(ruleKey2)
        assertThrows("The rule '$ruleKey2' hasn't been defined.", GrammarException::class.java) {
            b.build()
        }
    }

    @Test
    fun test_incorrect_type_of_parsing_expression() {
        assertThrows("Incorrect type of parsing expression: class java.lang.Object", IllegalArgumentException::class.java) {
            LexerfulGrammarBuilder.create().convertToExpression(Any())
        }
    }

    @Test
    fun should_fail_to_redefine() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        b.rule(ruleKey).`is`("foo")
        assertThrows("The rule '$ruleKey' has already been defined somewhere in the grammar.", GrammarException::class.java) {
            b.rule(ruleKey).`is`("foo")
        }
    }

    @Test
    fun should_fail_to_redefine2() {
        val b = LexerfulGrammarBuilder.create()
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        b.rule(ruleKey).`is`("foo", "bar")
        assertThrows("The rule '$ruleKey' has already been defined somewhere in the grammar.", GrammarException::class.java) {
            b.rule(ruleKey).`is`("foo")
        }
    }
}