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
package com.felipebz.flr.internal.grammar

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.internal.vm.ParsingExpression
import com.felipebz.flr.internal.vm.SequenceExpression
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MutableParsingRuleTest {
    @Test
    fun should_not_allow_redefinition() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = MutableParsingRule(ruleKey)
        rule.`is`(mock<ParsingExpression>())
        assertThrows<GrammarException>("The rule '$ruleKey' has already been defined somewhere in the grammar.") {
            rule.`is`(mock<ParsingExpression>())
        }
    }

    @Test
    fun should_override() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = MutableParsingRule(ruleKey)
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        rule.`is`(e1, e2)
        rule.override(e2)
        assertThat(rule.expression).isSameAs(e2)
        rule.override(e1, e2)
        assertThat(rule.expression).isInstanceOf(SequenceExpression::class.java)
    }

    @Test
    fun should_not_skip_from_AST() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = MutableParsingRule(ruleKey)
        val astNode = mock<AstNode>()
        assertThat(rule.hasToBeSkippedFromAst(astNode)).isFalse()
    }

    @Test
    fun should_skip_from_AST() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = MutableParsingRule(ruleKey)
        rule.skip()
        val astNode = mock<AstNode>()
        assertThat(rule.hasToBeSkippedFromAst(astNode)).isTrue()
    }

    @Test
    fun should_skip_from_AST_if_one_child() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = MutableParsingRule(ruleKey)
        rule.skipIfOneChild()
        val astNode = mock<AstNode>()
        whenever(astNode.numberOfChildren).thenReturn(1)
        assertThat(rule.hasToBeSkippedFromAst(astNode)).isTrue()
    }

    @Test
    fun should_return_real_AstNodeType() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = MutableParsingRule(ruleKey)
        assertThat(rule.getRealAstNodeType()).isSameAs(ruleKey)
    }
}