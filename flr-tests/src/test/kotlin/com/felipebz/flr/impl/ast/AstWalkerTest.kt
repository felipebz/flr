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
package com.felipebz.flr.impl.ast

import com.felipebz.flr.api.AstAndTokenVisitor
import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.AstVisitor
import com.felipebz.flr.impl.MockTokenType
import com.felipebz.flr.test.lexer.MockHelper.mockToken
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
    private val token = mockToken(MockTokenType.WORD, "word")
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
