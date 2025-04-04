/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.impl.matcher

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.test.lexer.MockHelper.mockToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class RuleDefinitionTest {
    @Test
    fun testEmptyIs() {
        val javaClassDefinition = RuleDefinition("JavaClassDefinition")
        val thrown = assertThrows<IllegalStateException> {
            javaClassDefinition.`is`()
        }
        assertEquals("The rule 'JavaClassDefinition' should at least contains one matcher.", thrown.message)
    }

    @Test
    fun testMoreThanOneDefinitionForASigleRuleWithIs() {
        val javaClassDefinition = RuleDefinition("JavaClassDefinition")
        javaClassDefinition.`is`("option1")
        val thrown = assertThrows<IllegalStateException> {
            javaClassDefinition.`is`("option2")
        }
        assertEquals(
            "The rule 'JavaClassDefinition' has already been defined somewhere in the grammar.",
            thrown.message
        )
    }

    @Test
    fun testSkipFromAst() {
        val ruleBuilder = RuleDefinition("MyRule")
        assertThat(ruleBuilder.hasToBeSkippedFromAst(mock())).isFalse()
        ruleBuilder.skip()
        assertThat(ruleBuilder.hasToBeSkippedFromAst(mock())).isTrue()
    }

    @Test
    fun testSkipFromAstIf() {
        val ruleBuilder = RuleDefinition("MyRule")
        ruleBuilder.skipIfOneChild()
        val parent = AstNode(mockToken(GenericTokenType.IDENTIFIER, "parent"))
        val child1 = AstNode(mockToken(GenericTokenType.IDENTIFIER, "child1"))
        val child2 = AstNode(mockToken(GenericTokenType.IDENTIFIER, "child2"))
        parent.addChild(child1)
        parent.addChild(child2)
        child1.addChild(child2)
        assertThat(ruleBuilder.hasToBeSkippedFromAst(parent)).isFalse()
        assertThat(ruleBuilder.hasToBeSkippedFromAst(child2)).isFalse()
        assertThat(ruleBuilder.hasToBeSkippedFromAst(child1)).isTrue()
    }
}
