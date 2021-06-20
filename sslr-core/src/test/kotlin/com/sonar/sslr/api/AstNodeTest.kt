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
package com.sonar.sslr.api

import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito

class AstNodeTest {
    /**
     * <pre>
     * A1
     * |- A2
     * |   \- B1
     * |- B2
     * |   \- C1
     * |- B3
     * \- C2
    </pre> *
     */
    @Test
    fun test() {
        val a = Mockito.mock(AstNodeType::class.java)
        val b = Mockito.mock(AstNodeType::class.java)
        val c = Mockito.mock(AstNodeType::class.java)
        val a1 = AstNode(a, "a1", null)
        val a2 = AstNode(a, "a2", null)
        val b1 = AstNode(b, "b1", null)
        val b2 = AstNode(b, "b2", null)
        val b3 = AstNode(b, "b3", null)
        val c1 = AstNode(c, "c1", null)
        val c2 = AstNode(c, "c2", null)
        a1.addChild(a2)
        a2.addChild(b1)
        a1.addChild(b2)
        b2.addChild(c1)
        a1.addChild(b3)
        a1.addChild(c2)
        Assertions.assertThat(a1.hasChildren()).isTrue()
        Assertions.assertThat(c1.hasChildren()).isFalse()
        Assertions.assertThat(a1.firstChild).isSameAs(a2)
        Assertions.assertThat(a1.lastChild).isSameAs(c2)
        Assertions.assertThat(a1.hasDirectChildren(Mockito.mock(AstNodeType::class.java))).isFalse()
        Assertions.assertThat(a1.hasDirectChildren(a)).isTrue()
        Assertions.assertThat(a1.hasDirectChildren(a, b)).isTrue()
        Assertions.assertThat(a1.getFirstChild(Mockito.mock(AstNodeType::class.java))).isNull()
        Assertions.assertThat(a1.getFirstChild(a)).isSameAs(a2)
        Assertions.assertThat(a1.getFirstChild(b)).isSameAs(b2)
        Assertions.assertThat(a1.getFirstChild(a, b)).isSameAs(a2)
        Assertions.assertThat(a1.getLastChild(Mockito.mock(AstNodeType::class.java))).isNull()
        Assertions.assertThat(a1.getLastChild(a)).isSameAs(a2)
        Assertions.assertThat(a1.getLastChild(b)).isSameAs(b3)
        Assertions.assertThat(a1.getLastChild(a, b)).isSameAs(b3)
        Assertions.assertThat(a1.getChildren(Mockito.mock(AstNodeType::class.java))).isEmpty()
        Assertions.assertThat(a1.getChildren(a)).containsExactly(a2)
        Assertions.assertThat(a1.getChildren(b)).containsExactly(b2, b3)
        Assertions.assertThat(a1.getChildren(a, b)).containsExactly(a2, b2, b3)
        Assertions.assertThat(a1.hasDescendant(Mockito.mock(AstNodeType::class.java))).isFalse()
        Assertions.assertThat(a1.hasDescendant(a)).isTrue()
        Assertions.assertThat(a1.hasDescendant(a, b)).isTrue()
        Assertions.assertThat(a1.getFirstDescendant(Mockito.mock(AstNodeType::class.java))).isNull()
        Assertions.assertThat(a1.getFirstDescendant(a)).isSameAs(a2)
        Assertions.assertThat(a1.getFirstDescendant(b)).isSameAs(b1)
        Assertions.assertThat(a1.getFirstDescendant(a, b)).isSameAs(a2)
        Assertions.assertThat(a1.getDescendants(Mockito.mock(AstNodeType::class.java))).isEmpty()
        Assertions.assertThat(a1.getDescendants(a)).containsExactly(a2)
        Assertions.assertThat(a1.getDescendants(b)).containsExactly(b1, b2, b3)
        Assertions.assertThat(a1.getDescendants(a, b)).containsExactly(a2, b1, b2, b3)
        Assertions.assertThat(a1.nextSibling).isNull()
        Assertions.assertThat(c1.nextSibling).isNull()
        Assertions.assertThat(b3.nextSibling).isSameAs(c2)
        Assertions.assertThat(a1.previousSibling).isNull()
        Assertions.assertThat(a2.previousSibling).isNull()
        Assertions.assertThat(b2.previousSibling).isSameAs(a2)
        Assertions.assertThat(a1.nextAstNode).isNull()
        Assertions.assertThat(b1.nextAstNode).isSameAs(b2)
        Assertions.assertThat(a1.previousAstNode).isNull()
        Assertions.assertThat(b2.previousAstNode).isSameAs(a2)
        Assertions.assertThat(c1.hasAncestor(Mockito.mock(AstNodeType::class.java))).isFalse()
        Assertions.assertThat(c1.hasAncestor(a)).isTrue()
        Assertions.assertThat(c1.hasAncestor(c)).isFalse()
        Assertions.assertThat(c1.hasAncestor(a, c)).isTrue()
        Assertions.assertThat(c1.getFirstAncestor(a)).isSameAs(a1)
        Assertions.assertThat(c1.getFirstAncestor(c)).isNull()
        Assertions.assertThat(c1.getFirstAncestor(a, c)).isSameAs(a1)
        Assertions.assertThat(c1.getFirstAncestor(c, c)).isNull()
        Assertions.assertThat(a1.hasParent()).isFalse()
        Assertions.assertThat(a2.hasParent(a)).isTrue()
        Assertions.assertThat(a2.hasParent(b)).isFalse()
        Assertions.assertThat(a2.hasParent(a, b)).isTrue()
    }

    /**
     * <pre>
     * root
     * |- empty
     * |- nonempty
     * \- intermediate empty
     * \- empty
    </pre> *
     */
    @Test
    fun test_getLastToken() {
        val token = Mockito.mock(Token::class.java)
        val a = Mockito.mock(AstNodeType::class.java)
        val rootNode = AstNode(a, "root", token)
        val firstEmptyNode = AstNode(a, "empty", null)
        val nonemptyNode = AstNode(a, "nonempty", token)
        val intermediateEmptyNode = AstNode(a, "intermediate empty", null)
        val lastEmptyNode = AstNode(a, "empty", null)
        rootNode.addChild(firstEmptyNode)
        rootNode.addChild(nonemptyNode)
        rootNode.addChild(intermediateEmptyNode)
        intermediateEmptyNode.addChild(lastEmptyNode)
        Assertions.assertThat(rootNode.lastToken).isSameAs(token)
        Assertions.assertThat(firstEmptyNode.lastToken).isNull()
        Assertions.assertThat(intermediateEmptyNode.lastToken).isNull()
        Assertions.assertThat(lastEmptyNode.lastToken).isNull()
    }

    @Test
    fun test_getTokens() {
        val token = Mockito.mock(Token::class.java)
        val a = Mockito.mock(AstNodeType::class.java)
        val rootNode = AstNode(a, "root", token)
        val firstEmptyNode = AstNode(a, "empty", null)
        val nonemptyNode = AstNode(a, "nonempty", token)
        val lastEmptyNode = AstNode(a, "empty", null)
        rootNode.addChild(firstEmptyNode)
        rootNode.addChild(nonemptyNode)
        rootNode.addChild(lastEmptyNode)
        Assertions.assertThat(rootNode.tokens).containsExactly(token)
        Assertions.assertThat(firstEmptyNode.tokens).isEmpty()
        Assertions.assertThat(nonemptyNode.tokens).containsExactly(token)
        Assertions.assertThat(lastEmptyNode.tokens).isEmpty()
    }

    @Test
    fun test_toString() {
        val token = Mockito.mock(Token::class.java)
        Mockito.`when`(token.value).thenReturn("foo")
        Mockito.`when`(token.line).thenReturn(42)
        Mockito.`when`(token.column).thenReturn(24)
        var node = AstNode(Mockito.mock(AstNodeType::class.java), "node_name", token)
        Assertions.assertThat(node.toString()).isEqualTo("node_name tokenValue='foo' tokenLine=42 tokenColumn=24")
        node = AstNode(Mockito.mock(AstNodeType::class.java), "node_name", null)
        Assertions.assertThat(node.toString()).isEqualTo("node_name")
    }
}