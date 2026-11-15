//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.grammar

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.grammar.GrammarRuleKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MutableParsingRuleTest {
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
