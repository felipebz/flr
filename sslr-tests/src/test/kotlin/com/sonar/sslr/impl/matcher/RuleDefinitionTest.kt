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
package com.sonar.sslr.impl.matcher

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.test.lexer.MockHelper.mockToken
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock

class RuleDefinitionTest {
    @Test
    fun testEmptyIs() {
        val javaClassDefinition = RuleDefinition("JavaClassDefinition")
        val thrown = Assert.assertThrows(
            IllegalStateException::class.java
        ) { javaClassDefinition.`is`() }
        Assert.assertEquals("The rule 'JavaClassDefinition' should at least contains one matcher.", thrown.message)
    }

    @Test
    fun testMoreThanOneDefinitionForASigleRuleWithIs() {
        val javaClassDefinition = RuleDefinition("JavaClassDefinition")
        javaClassDefinition.`is`("option1")
        val thrown = Assert.assertThrows(
            IllegalStateException::class.java
        ) { javaClassDefinition.`is`("option2") }
        Assert.assertEquals(
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