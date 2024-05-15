/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.impl.typed

import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.ParsingExpression
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

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
    fun should_fail_when_method_is_not_mapped() {
        assertThrows<IllegalStateException>("Cannot find the rule key corresponding to the invoked method: FOO()") {
            val method = DelayedRuleInvocationExpressionTest::class.java.getDeclaredMethod("FOO")
            DelayedRuleInvocationExpression(LexerlessGrammarBuilder.create(), mock(), method).compile(mock())
        }
    }

    @Test
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