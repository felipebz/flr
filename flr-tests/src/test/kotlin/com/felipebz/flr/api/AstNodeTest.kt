/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.api

import com.felipebz.flr.test.lexer.MockHelper.mockToken
import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.test.minic.MiniCParser.parseString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AstNodeTest {
    @Test
    fun testAddChild() {
        val expr = AstNode(NodeType(), "expr", null)
        val stat = AstNode(NodeType(), "stat", null)
        val assign = AstNode(NodeType(), "assign", null)
        expr.addChild(stat)
        expr.addChild(assign)
        assertThat(expr.children).contains(stat, assign)
    }

    @Test
    fun testAddNullChild() {
        val expr = AstNode(NodeType(), "expr", null)
        expr.addChild(null)
        assertThat(expr.hasChildren()).isFalse()
    }

    @Test
    fun testAddChildWhichMustBeSkippedFromAst() {
        val expr = AstNode(NodeType(), "expr", null)
        val all = AstNode(NodeType(true), "all", null)
        val stat = AstNode(NodeType(), "stat", null)
        all.addChild(stat)
        expr.addChild(all)
        val many = AstNode(NodeType(true), "many", null)
        val print = AstNode(NodeType(), "print", null)
        many.addChild(print)
        expr.addChild(many)
        assertThat(expr.children).contains(stat, print)
    }

    @Test
    fun testAddMatcherChildWithoutChildren() {
        val expr = AstNode(NodeType(), "expr", null)
        val all = AstNode(NodeType(true), "all", null)
        expr.addChild(all)
        assertThat(expr.children.size).isEqualTo(0)
    }

    @Test
    fun testHasChildren() {
        val expr = AstNode(NodeType(), "expr", null)
        assertThat(expr.hasChildren()).isFalse()
    }

    @Test
    fun testGetLastToken() {
        val lastToken = mockToken(GenericTokenType.IDENTIFIER, "LAST_TOKEN")
        val parent = AstNode(NodeType(), "parent", lastToken)
        val child1 = AstNode(NodeType(), "child1", null)
        val child2 = AstNode(NodeType(), "child2", lastToken)
        parent.addChild(child1)
        parent.addChild(child2)
        assertThat(parent.lastToken).isSameAs(lastToken)
        assertThat(child2.lastToken).isSameAs(lastToken)
    }

    @Test
    fun testGetTokens() {
        val child1Token = mockToken(GenericTokenType.IDENTIFIER, "CHILD 1")
        val child2Token = mockToken(GenericTokenType.IDENTIFIER, "CHILD 2")
        val parent = AstNode(NodeType(), "parent", null)
        val child1 = AstNode(NodeType(), "child1", child1Token)
        val child2 = AstNode(NodeType(), "child2", child2Token)
        parent.addChild(child1)
        parent.addChild(child2)
        assertThat(parent.tokens.size).isEqualTo(2)
        assertThat(parent.tokens[0]).isSameAs(child1Token)
        assertThat(parent.tokens[1]).isSameAs(child2Token)
    }

    @Test
    fun testNextSibling() {
        val expr1 = AstNode(NodeType(), "expr1", null)
        val expr2 = AstNode(NodeType(), "expr2", null)
        val statement = AstNode(NodeType(), "statement", null)
        statement.addChild(expr1)
        statement.addChild(expr2)
        assertThat(expr1.nextSiblingOrNull).isSameAs(expr2)
        assertThat(expr2.nextSiblingOrNull).isNull()
    }

    @Test
    fun testPreviousSibling() {
        val expr1 = AstNode(NodeType(), "expr1", null)
        val expr2 = AstNode(NodeType(), "expr2", null)
        val statement = AstNode(NodeType(), "statement", null)
        statement.addChild(expr1)
        statement.addChild(expr2)
        assertThat(expr1.previousSiblingOrNull).isNull()
        assertThat(expr2.previousSiblingOrNull).isSameAs(expr1)
    }

    @Test
    fun testIs() {
        val declarationNode = checkNotNull(parseString("int a = 0;").firstChild)
        assertThat(declarationNode.`is`(MiniCGrammar.DEFINITION)).isTrue()
        assertThat(declarationNode.`is`(MiniCGrammar.COMPILATION_UNIT, MiniCGrammar.DEFINITION)).isTrue()
        assertThat(declarationNode.`is`(MiniCGrammar.DEFINITION, MiniCGrammar.COMPILATION_UNIT)).isTrue()
        assertThat(declarationNode.`is`(MiniCGrammar.COMPILATION_UNIT)).isFalse()
    }

    @Test
    fun testIsNot() {
        val declarationNode = checkNotNull(parseString("int a = 0;").firstChild)
        assertThat(declarationNode.isNot(MiniCGrammar.DEFINITION)).isFalse()
        assertThat(declarationNode.isNot(MiniCGrammar.COMPILATION_UNIT, MiniCGrammar.DEFINITION)).isFalse()
        assertThat(declarationNode.isNot(MiniCGrammar.DEFINITION, MiniCGrammar.COMPILATION_UNIT)).isFalse()
        assertThat(declarationNode.isNot(MiniCGrammar.COMPILATION_UNIT)).isTrue()
    }

    @Test
    fun testGetLastChild() {
        val expr1 = AstNode(NodeType(), "expr1", null)
        val expr2 = AstNode(NodeType(), "expr2", null)
        val statement = AstNode(NodeType(), "statement", null)
        statement.addChild(expr1)
        statement.addChild(expr2)
        assertThat(statement.lastChild).isSameAs(expr2)
    }

    /**
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ D1
     * |__ B3
    </pre> *
     */
    @Test
    fun test_getDescendants() {
        val a = NodeType()
        val b = NodeType()
        val c = NodeType()
        val d = NodeType()
        val e = NodeType()
        val a1 = AstNode(a, "a1", null)
        val c1 = AstNode(c, "c1", null)
        val b1 = AstNode(b, "b1", null)
        val b2 = AstNode(b, "b2", null)
        val d1 = AstNode(d, "d1", null)
        val b3 = AstNode(b, "b3", null)
        a1.addChild(c1)
        c1.addChild(b1)
        a1.addChild(b2)
        a1.addChild(d1)
        a1.addChild(b3)
        assertThat(a1.getDescendants(b, c)).containsExactly(c1, b1, b2, b3)
        assertThat(a1.getDescendants(b)).containsExactly(b1, b2, b3)
        assertThat(a1.getDescendants(e)).isEmpty()
        assertThat(a1.getDescendants(a)).`as`("SSLR-249").isEmpty()
    }

    private inner class NodeType : AstNodeSkippingPolicy {
        private var skippedFromAst = false

        constructor()
        constructor(skippedFromAst: Boolean) {
            this.skippedFromAst = skippedFromAst
        }

        override fun hasToBeSkippedFromAst(node: AstNode): Boolean {
            return skippedFromAst
        }
    }
}