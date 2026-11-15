//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.grammar

import com.felipebz.flr.grammar.GrammarBuilder.RuleBuilder
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.internal.vm.ParsingExpression
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.*

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
        assertThrows<GrammarException>("The rule '$ruleKey' has already been defined somewhere in the grammar.") {
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
