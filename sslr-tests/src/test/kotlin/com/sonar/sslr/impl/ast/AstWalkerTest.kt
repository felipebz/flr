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
package com.sonar.sslr.impl.ast

import com.sonar.sslr.api.AstAndTokenVisitor
import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeType
import com.sonar.sslr.api.AstVisitor
import com.sonar.sslr.impl.MockTokenType
import com.sonar.sslr.test.lexer.MockHelper.mockToken
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*

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
    private val astVisitor = Mockito.mock(AstVisitor::class.java)
    private val astAndTokenVisitor = Mockito.mock(AstAndTokenVisitor::class.java)
    @Before
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
        Mockito.`when`(astVisitor.getAstNodeTypesToVisit()).thenReturn(ArrayList())
        walker.addVisitor(astVisitor)
        walker.walkAndVisit(ast1)
        Mockito.verify(astVisitor).visitFile(ast1)
        Mockito.verify(astVisitor).leaveFile(ast1)
        Mockito.verify(astVisitor, Mockito.never()).visitNode(ast11)
    }

    @Test
    fun testVisitToken() {
        Mockito.`when`(astAndTokenVisitor.getAstNodeTypesToVisit()).thenReturn(ArrayList())
        walker.addVisitor(astAndTokenVisitor)
        walker.walkAndVisit(astNodeWithToken)
        Mockito.verify(astAndTokenVisitor).visitFile(astNodeWithToken)
        Mockito.verify(astAndTokenVisitor).leaveFile(astNodeWithToken)
        Mockito.verify(astAndTokenVisitor).visitToken(token)
    }

    @Test
    fun testVisitNodeAndLeaveNodeCalls() {
        Mockito.`when`(astVisitor.getAstNodeTypesToVisit()).thenReturn(listOf(tiger))
        walker.addVisitor(astVisitor)
        walker.walkAndVisit(ast1)
        val inOrder = Mockito.inOrder(astVisitor)
        inOrder.verify(astVisitor).visitNode(ast122)
        inOrder.verify(astVisitor).leaveNode(ast122)
        Mockito.verify(astVisitor, Mockito.never()).visitNode(ast11)
    }

    @Test
    fun testAddVisitor() {
        val walker = AstWalker()
        val astNodeType = Mockito.mock(AstNodeType::class.java)
        val visitor1 = Mockito.mock(AstVisitor::class.java)
        Mockito.`when`(visitor1.getAstNodeTypesToVisit()).thenReturn(listOf(astNodeType))
        val visitor2 = Mockito.mock(AstVisitor::class.java)
        Mockito.`when`(visitor2.getAstNodeTypesToVisit()).thenReturn(listOf(astNodeType))
        walker.addVisitor(visitor1)
        walker.addVisitor(visitor2)
    }
}