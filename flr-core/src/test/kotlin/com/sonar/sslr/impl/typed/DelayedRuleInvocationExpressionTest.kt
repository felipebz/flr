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
package com.sonar.sslr.impl.typed

import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.*
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.grammar.LexerlessGrammarBuilder
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.internal.vm.CompilationHandler
import org.sonar.sslr.internal.vm.ParsingExpression

class DelayedRuleInvocationExpressionTest {
    @Test
    fun should_compile_rule_keys() {
        val b = spy(LexerlessGrammarBuilder.create())
        val ruleKey = mock<GrammarRuleKey>()
        val expression = DelayedRuleInvocationExpression(b, ruleKey)
        val compiler = mock<CompilationHandler>()
        expression.compile(compiler)
        verify(b).rule(ruleKey)
        val ruleExpression = argumentCaptor<ParsingExpression>()
        verify(compiler).compile(ruleExpression.capture())
        assertThat(ruleExpression.allValues).hasSize(1)
        assertThat((ruleExpression.firstValue as MutableParsingRule).ruleKey).isSameAs(ruleKey)
    }

    @Test
    @Throws(Exception::class)
    fun should_compile_methods() {
        val b = spy(LexerlessGrammarBuilder.create())
        val ruleKey = mock<GrammarRuleKey>()
        val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
        val grammarBuilderInterceptor = mock<GrammarBuilderInterceptor<Any>>()
        whenever(grammarBuilderInterceptor.ruleKeyForMethod(method)).thenReturn(ruleKey)
        val expression = DelayedRuleInvocationExpression(b, grammarBuilderInterceptor, method)
        val compiler = mock<CompilationHandler>()
        expression.compile(compiler)
        verify(b).rule(ruleKey)
        val ruleExpression =    argumentCaptor<ParsingExpression>()
        verify(compiler).compile(ruleExpression.capture())
        assertThat(ruleExpression.allValues).hasSize(1)
        assertThat((ruleExpression.firstValue as MutableParsingRule).ruleKey).isSameAs(ruleKey)
    }

    @Test
    @Throws(Exception::class)
    fun should_fail_when_method_is_not_mapped() {
        assertThrows("Cannot find the rule key corresponding to the invoked method: FOO()", IllegalStateException::class.java) {
            val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
            DelayedRuleInvocationExpression(LexerlessGrammarBuilder.create(), mock(), method).compile(mock())
        }
    }

    @Test
    @Throws(Exception::class)
    fun test_toString() {
        val ruleKey = mock<GrammarRuleKey>()
        whenever(ruleKey.toString()).thenReturn("foo")
        assertThat(
            DelayedRuleInvocationExpression(mock(), ruleKey).toString()
        ).isEqualTo("foo")
        val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
        assertThat(
            DelayedRuleInvocationExpression(mock(), mock(), method).toString()
        ).isEqualTo("FOO()")
    }

    // Called by reflection
    fun FOO() {}
}