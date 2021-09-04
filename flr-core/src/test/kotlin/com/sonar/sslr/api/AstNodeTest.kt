/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package com.sonar.sslr.api

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        val a = mock<AstNodeType>()
        val b = mock<AstNodeType>()
        val c = mock<AstNodeType>()
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
        assertThat(a1.hasChildren()).isTrue()
        assertThat(c1.hasChildren()).isFalse()
        assertThat(a1.firstChild).isSameAs(a2)
        assertThat(a1.lastChild).isSameAs(c2)
        assertThat(a1.hasDirectChildren(mock())).isFalse()
        assertThat(a1.hasDirectChildren(a)).isTrue()
        assertThat(a1.hasDirectChildren(a, b)).isTrue()
        assertThat(a1.getFirstChildOrNull(mock())).isNull()
        assertThat(a1.getFirstChild(a)).isSameAs(a2)
        assertThat(a1.getFirstChild(b)).isSameAs(b2)
        assertThat(a1.getFirstChild(a, b)).isSameAs(a2)
        assertThat(a1.getFirstChildOrNull(mock())).isNull()
        assertThat(a1.getLastChild(a)).isSameAs(a2)
        assertThat(a1.getLastChild(b)).isSameAs(b3)
        assertThat(a1.getLastChild(a, b)).isSameAs(b3)
        assertThat(a1.getChildren(mock())).isEmpty()
        assertThat(a1.getChildren(a)).containsExactly(a2)
        assertThat(a1.getChildren(b)).containsExactly(b2, b3)
        assertThat(a1.getChildren(a, b)).containsExactly(a2, b2, b3)
        assertThat(a1.hasDescendant(mock())).isFalse()
        assertThat(a1.hasDescendant(a)).isTrue()
        assertThat(a1.hasDescendant(a, b)).isTrue()
        assertThat(a1.getFirstDescendantOrNull(mock())).isNull()
        assertThat(a1.getFirstDescendant(a)).isSameAs(a2)
        assertThat(a1.getFirstDescendant(b)).isSameAs(b1)
        assertThat(a1.getFirstDescendant(a, b)).isSameAs(a2)
        assertThat(a1.getDescendants(mock())).isEmpty()
        assertThat(a1.getDescendants(a)).containsExactly(a2)
        assertThat(a1.getDescendants(b)).containsExactly(b1, b2, b3)
        assertThat(a1.getDescendants(a, b)).containsExactly(a2, b1, b2, b3)
        assertThat(a1.nextSiblingOrNull).isNull()
        assertThat(c1.nextSiblingOrNull).isNull()
        assertThat(b3.nextSiblingOrNull).isSameAs(c2)
        assertThat(a1.previousSiblingOrNull).isNull()
        assertThat(a2.previousSiblingOrNull).isNull()
        assertThat(b2.previousSiblingOrNull).isSameAs(a2)
        assertThat(a1.nextAstNodeOrNUll).isNull()
        assertThat(b1.nextAstNodeOrNUll).isSameAs(b2)
        assertThat(a1.previousAstNodeOrNull).isNull()
        assertThat(b2.previousAstNodeOrNull).isSameAs(a2)
        assertThat(c1.hasAncestor(mock())).isFalse()
        assertThat(c1.hasAncestor(a)).isTrue()
        assertThat(c1.hasAncestor(c)).isFalse()
        assertThat(c1.hasAncestor(a, c)).isTrue()
        assertThat(c1.getFirstAncestorOrNull(a)).isSameAs(a1)
        assertThat(c1.getFirstAncestorOrNull(c)).isNull()
        assertThat(c1.getFirstAncestorOrNull(a, c)).isSameAs(a1)
        assertThat(c1.getFirstAncestorOrNull(c, c)).isNull()
        assertThat(a1.hasParent()).isFalse()
        assertThat(a2.hasParent(a)).isTrue()
        assertThat(a2.hasParent(b)).isFalse()
        assertThat(a2.hasParent(a, b)).isTrue()
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
        val token = mock<Token>()
        val a = mock<AstNodeType>()
        val rootNode = AstNode(a, "root", token)
        val firstEmptyNode = AstNode(a, "empty", null)
        val nonemptyNode = AstNode(a, "nonempty", token)
        val intermediateEmptyNode = AstNode(a, "intermediate empty", null)
        val lastEmptyNode = AstNode(a, "empty", null)
        rootNode.addChild(firstEmptyNode)
        rootNode.addChild(nonemptyNode)
        rootNode.addChild(intermediateEmptyNode)
        intermediateEmptyNode.addChild(lastEmptyNode)
        assertThat(rootNode.lastTokenOrNull).isSameAs(token)
        assertThat(firstEmptyNode.lastTokenOrNull).isNull()
        assertThat(intermediateEmptyNode.lastTokenOrNull).isNull()
        assertThat(lastEmptyNode.lastTokenOrNull).isNull()
    }

    @Test
    fun test_getTokens() {
        val token = mock<Token>()
        val a = mock<AstNodeType>()
        val rootNode = AstNode(a, "root", token)
        val firstEmptyNode = AstNode(a, "empty", null)
        val nonemptyNode = AstNode(a, "nonempty", token)
        val lastEmptyNode = AstNode(a, "empty", null)
        rootNode.addChild(firstEmptyNode)
        rootNode.addChild(nonemptyNode)
        rootNode.addChild(lastEmptyNode)
        assertThat(rootNode.tokens).containsExactly(token)
        assertThat(firstEmptyNode.tokens).isEmpty()
        assertThat(nonemptyNode.tokens).containsExactly(token)
        assertThat(lastEmptyNode.tokens).isEmpty()
    }

    @Test
    fun test_toString() {
        val token = mock<Token>()
        whenever(token.value).thenReturn("foo")
        whenever(token.line).thenReturn(42)
        whenever(token.column).thenReturn(24)
        var node = AstNode(mock(), "node_name", token)
        assertThat(node.toString()).isEqualTo("node_name tokenValue='foo' tokenLine=42 tokenColumn=24")
        node = AstNode(mock(), "node_name", null)
        assertThat(node.toString()).isEqualTo("node_name")
    }
}