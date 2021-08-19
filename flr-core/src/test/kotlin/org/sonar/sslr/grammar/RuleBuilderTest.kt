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

import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.sonar.sslr.grammar.GrammarBuilder.RuleBuilder
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.vm.CompilableGrammarRule
import org.sonar.sslr.internal.vm.ParsingExpression

class RuleBuilderTest {

    private val b = mock<GrammarBuilder>(defaultAnswer = Mockito.CALLS_REAL_METHODS)
    private val delegate = mock<CompilableGrammarRule>()
    private val ruleBuilder = RuleBuilder(b, delegate)
    @Test
    fun test_is() {
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        whenever(b.convertToExpression(e1)).thenReturn(e2)
        ruleBuilder.`is`(e1)
        verify(delegate).expression = e2
    }

    @Test
    fun test_is2() {
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        val e3 = mock<ParsingExpression>()
        whenever(b.convertToExpression(any())).thenReturn(e3)
        ruleBuilder.`is`(e1, e2)
        verify(delegate).expression = e3
    }

    @Test
    fun should_fail_to_redefine() {
        val e = mock<ParsingExpression>()
        whenever(delegate.expression).thenReturn(e)
        val ruleKey = mock<GrammarRuleKey>()
        whenever(delegate.ruleKey).thenReturn(ruleKey)
        assertThrows("The rule '$ruleKey' has already been defined somewhere in the grammar.", GrammarException::class.java) {
            ruleBuilder.`is`(e)
        }
    }

    @Test
    fun test_override() {
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        val e3 = mock<ParsingExpression>()
        whenever(b.convertToExpression(e1)).thenReturn(e1)
        whenever(b.convertToExpression(e2)).thenReturn(e3)
        ruleBuilder.`is`(e1)
        ruleBuilder.override(e2)
        val inOrder = inOrder(delegate)
        inOrder.verify(delegate).expression = e1
        inOrder.verify(delegate).expression = e3
    }

    @Test
    fun test_override2() {
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        val e3 = mock<ParsingExpression>()
        whenever(b.convertToExpression(e1)).thenReturn(e1)
        ruleBuilder.`is`(e1)
        verify(delegate).expression = e1
        whenever(b.convertToExpression(any())).thenReturn(e3)
        ruleBuilder.override(e1, e2)
        verify(delegate).expression = e3
    }

    @Test
    fun test_skip() {
        ruleBuilder.skip()
        verify(delegate).skip()
    }

    @Test
    fun test_skipIfOneChild() {
        ruleBuilder.skipIfOneChild()
        verify(delegate).skipIfOneChild()
    }
}