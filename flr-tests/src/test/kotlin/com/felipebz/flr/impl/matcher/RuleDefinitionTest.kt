//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.matcher

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.test.testToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class RuleDefinitionTest {
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
        val parent = AstNode(testToken(GenericTokenType.IDENTIFIER, "parent"))
        val child1 = AstNode(testToken(GenericTokenType.IDENTIFIER, "child1"))
        val child2 = AstNode(testToken(GenericTokenType.IDENTIFIER, "child2"))
        parent.addChild(child1)
        parent.addChild(child2)
        child1.addChild(child2)
        assertThat(ruleBuilder.hasToBeSkippedFromAst(parent)).isFalse()
        assertThat(ruleBuilder.hasToBeSkippedFromAst(child2)).isFalse()
        assertThat(ruleBuilder.hasToBeSkippedFromAst(child1)).isTrue()
    }
}
