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
package org.sonar.sslr.internal.grammar

import com.sonar.sslr.api.AstNode
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.vm.ParsingExpression
import org.sonar.sslr.internal.vm.SequenceExpression

class MutableParsingRuleTest {
    @Test
    fun should_not_allow_redefinition() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val rule = MutableParsingRule(ruleKey)
        rule.`is`(Mockito.mock(ParsingExpression::class.java))
        assertThrows("The rule '$ruleKey' has already been defined somewhere in the grammar.", GrammarException::class.java) {
            rule.`is`(Mockito.mock(ParsingExpression::class.java))
        }
    }

    @Test
    fun should_override() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val rule = MutableParsingRule(ruleKey)
        val e1 = Mockito.mock(ParsingExpression::class.java)
        val e2 = Mockito.mock(ParsingExpression::class.java)
        rule.`is`(e1, e2)
        rule.override(e2)
        Assertions.assertThat(rule.expression).isSameAs(e2)
        rule.override(e1, e2)
        Assertions.assertThat(rule.expression).isInstanceOf(SequenceExpression::class.java)
    }

    @Test
    fun should_not_skip_from_AST() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val rule = MutableParsingRule(ruleKey)
        val astNode = Mockito.mock(AstNode::class.java)
        Assertions.assertThat(rule.hasToBeSkippedFromAst(astNode)).isFalse()
    }

    @Test
    fun should_skip_from_AST() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val rule = MutableParsingRule(ruleKey)
        rule.skip()
        val astNode = Mockito.mock(AstNode::class.java)
        Assertions.assertThat(rule.hasToBeSkippedFromAst(astNode)).isTrue()
    }

    @Test
    fun should_skip_from_AST_if_one_child() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val rule = MutableParsingRule(ruleKey)
        rule.skipIfOneChild()
        val astNode = Mockito.mock(AstNode::class.java)
        Mockito.`when`(astNode.numberOfChildren).thenReturn(1)
        Assertions.assertThat(rule.hasToBeSkippedFromAst(astNode)).isTrue()
    }

    @Test
    fun should_return_real_AstNodeType() {
        val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
        val rule = MutableParsingRule(ruleKey)
        Assertions.assertThat(rule.getRealAstNodeType()).isSameAs(ruleKey)
    }
}