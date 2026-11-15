//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.ast

import com.felipebz.flr.api.AstAndTokenVisitor
import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.AstVisitor
import com.felipebz.flr.impl.MockTokenType
import com.felipebz.flr.test.testToken
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class AstWalkerTest {
    private val walker = AstWalker()
    private lateinit var ast1: AstNode
    private lateinit var ast11: AstNode
    private lateinit var ast12: AstNode
    private lateinit var ast121: AstNode
    private lateinit var ast122: AstNode
    private lateinit var ast13: AstNode
    private lateinit var astNodeWithToken: AstNode
    private val token = testToken(MockTokenType.WORD, "word")
    private val animal: AstNodeType = object : AstNodeType {}
    private val dog: AstNodeType = object : AstNodeType {}
    private val cat: AstNodeType = object : AstNodeType {}
    private val tiger: AstNodeType = object : AstNodeType {}
    private val astVisitor = mock<AstVisitor>()
    private val astAndTokenVisitor = mock<AstAndTokenVisitor>()
    @BeforeEach
    fun init() {
        ast121 = AstNode(animal, "121", null)
        ast122 = AstNode(tiger, "122", null)
        ast11 = AstNode(dog, "11", null)
        ast12 = AstNode(animal, "12", null).apply {
            addChild(ast121)
            addChild(ast122)
        }
        ast13 = AstNode(cat, "13", null)
        ast1 = AstNode(animal, "1", null).apply {
            addChild(ast11)
            addChild(ast12)
            addChild(ast13)
        }
        astNodeWithToken = AstNode(token)
    }

    @Test
    fun testVisitFileAndLeaveFileCalls() {
        whenever(astVisitor.getAstNodeTypesToVisit()).thenReturn(ArrayList())
        walker.addVisitor(astVisitor)
        walker.walkAndVisit(ast1)
        verify(astVisitor).visitFile(ast1)
        verify(astVisitor).leaveFile(ast1)
        verify(astVisitor, never()).visitNode(ast11)
    }

    @Test
    fun testVisitToken() {
        whenever(astAndTokenVisitor.getAstNodeTypesToVisit()).thenReturn(ArrayList())
        walker.addVisitor(astAndTokenVisitor)
        walker.walkAndVisit(astNodeWithToken)
        verify(astAndTokenVisitor).visitFile(astNodeWithToken)
        verify(astAndTokenVisitor).leaveFile(astNodeWithToken)
        verify(astAndTokenVisitor).visitToken(token)
    }

    @Test
    fun testVisitNodeAndLeaveNodeCalls() {
        whenever(astVisitor.getAstNodeTypesToVisit()).thenReturn(listOf(tiger))
        walker.addVisitor(astVisitor)
        walker.walkAndVisit(ast1)
        val inOrder = inOrder(astVisitor)
        inOrder.verify(astVisitor).visitNode(ast122)
        inOrder.verify(astVisitor).leaveNode(ast122)
        verify(astVisitor, never()).visitNode(ast11)
    }

    @Test
    fun testAddVisitor() {
        val walker = AstWalker()
        val astNodeType = mock<AstNodeType>()
        val visitor1 = mock<AstVisitor>()
        whenever(visitor1.getAstNodeTypesToVisit()).thenReturn(listOf(astNodeType))
        val visitor2 = mock<AstVisitor>()
        whenever(visitor2.getAstNodeTypesToVisit()).thenReturn(listOf(astNodeType))
        walker.addVisitor(visitor1)
        walker.addVisitor(visitor2)
    }
}
