/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package com.sonar.sslr.impl.typed

import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.grammar.LexerlessGrammarBuilder
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.internal.vm.CompilationHandler
import org.sonar.sslr.internal.vm.ParsingExpression

class DelayedRuleInvocationExpressionTest {
    @Test
    fun should_compile_rule_keys() {
        val b = Mockito.spy(LexerlessGrammarBuilder.create())
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val expression = DelayedRuleInvocationExpression(b, ruleKey)
        val compiler = Mockito.mock(CompilationHandler::class.java)
        expression.compile(compiler)
        Mockito.verify(b).rule(ruleKey)
        val ruleExpression = argumentCaptor<ParsingExpression>()
        Mockito.verify(compiler).compile(ruleExpression.capture())
        Assertions.assertThat(ruleExpression.allValues).hasSize(1)
        Assertions.assertThat((ruleExpression.firstValue as MutableParsingRule).ruleKey).isSameAs(ruleKey)
    }

    @Test
    @Throws(Exception::class)
    fun should_compile_methods() {
        val b = Mockito.spy(LexerlessGrammarBuilder.create())
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
        val grammarBuilderInterceptor = Mockito.mock(
            GrammarBuilderInterceptor::class.java
        )
        Mockito.`when`(grammarBuilderInterceptor.ruleKeyForMethod(method)).thenReturn(ruleKey)
        val expression = DelayedRuleInvocationExpression(b, grammarBuilderInterceptor, method)
        val compiler = Mockito.mock(CompilationHandler::class.java)
        expression.compile(compiler)
        Mockito.verify(b).rule(ruleKey)
        val ruleExpression =    argumentCaptor<ParsingExpression>()
        Mockito.verify(compiler).compile(ruleExpression.capture())
        Assertions.assertThat(ruleExpression.allValues).hasSize(1)
        Assertions.assertThat((ruleExpression.firstValue as MutableParsingRule).ruleKey).isSameAs(ruleKey)
    }

    @Test
    @Throws(Exception::class)
    fun should_fail_when_method_is_not_mapped() {
        assertThrows("Cannot find the rule key corresponding to the invoked method: FOO()", IllegalStateException::class.java) {
            val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
            DelayedRuleInvocationExpression(
                LexerlessGrammarBuilder.create(), Mockito.mock(
                    GrammarBuilderInterceptor::class.java
                ), method
            ).compile(Mockito.mock(CompilationHandler::class.java))
        }
    }

    @Test
    @Throws(Exception::class)
    fun test_toString() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        Mockito.`when`(ruleKey.toString()).thenReturn("foo")
        Assertions.assertThat(
            DelayedRuleInvocationExpression(
                Mockito.mock(
                    LexerlessGrammarBuilder::class.java
                ), ruleKey
            ).toString()
        ).isEqualTo("foo")
        val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
        Assertions.assertThat(
            DelayedRuleInvocationExpression(
                Mockito.mock(
                    LexerlessGrammarBuilder::class.java
                ), Mockito.mock(GrammarBuilderInterceptor::class.java), method
            ).toString()
        ).isEqualTo("FOO()")
    }

    // Called by reflection
    fun FOO() {}
}