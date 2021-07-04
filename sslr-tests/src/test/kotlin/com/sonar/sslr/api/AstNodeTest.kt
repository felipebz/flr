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

import com.sonar.sslr.test.lexer.MockHelper.mockToken
import com.sonar.sslr.test.minic.MiniCGrammar
import com.sonar.sslr.test.minic.MiniCParser.parseString
import org.fest.assertions.Assertions.assertThat
import org.junit.Test

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
    fun testGetChild() {
        val parent = AstNode(NodeType(), "parent", null)
        val child1 = AstNode(NodeType(), "child1", null)
        val child2 = AstNode(NodeType(), "child2", null)
        parent.addChild(child1)
        parent.addChild(child2)
        assertThat(parent.getChild(0)).isSameAs(child1)
        assertThat(parent.getChild(1)).isSameAs(child2)
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

    @Test(expected = IllegalStateException::class)
    fun testGetChildWithBadIndex() {
        val parent = AstNode(NodeType(), "parent", mockToken(GenericTokenType.IDENTIFIER, "PI"))
        val child1 = AstNode(NodeType(), "child1", null)
        parent.addChild(child1)
        parent.getChild(1)
    }

    @Test
    fun testNextSibling() {
        val expr1 = AstNode(NodeType(), "expr1", null)
        val expr2 = AstNode(NodeType(), "expr2", null)
        val statement = AstNode(NodeType(), "statement", null)
        statement.addChild(expr1)
        statement.addChild(expr2)
        assertThat(expr1.nextSibling()).isSameAs(expr2)
        assertThat(expr2.nextSibling()).isNull()
    }

    @Test
    fun testPreviousSibling() {
        val expr1 = AstNode(NodeType(), "expr1", null)
        val expr2 = AstNode(NodeType(), "expr2", null)
        val statement = AstNode(NodeType(), "statement", null)
        statement.addChild(expr1)
        statement.addChild(expr2)
        assertThat(expr1.previousSibling()).isNull()
        assertThat(expr2.previousSibling()).isSameAs(expr1)
    }

    @Test
    fun testFindFirstDirectChild() {
        val expr = AstNode(NodeType(), "expr", null)
        val statRule = NodeType()
        val stat = AstNode(statRule, "stat", null)
        val identifier = AstNode(NodeType(), "identifier", null)
        expr.addChild(stat)
        expr.addChild(identifier)
        assertThat(expr.findFirstDirectChild(statRule)).isSameAs(stat)
        val anotherRule = NodeType()
        assertThat(expr.findFirstDirectChild(anotherRule, statRule)).isSameAs(stat)
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
    fun testFindChildren() {
        val fileNode = parseString("int a = 0; int myFunction() { int b = 0; { int c = 0; } }")
        val binVariableDeclarationNodes = fileNode.findChildren(MiniCGrammar.BIN_VARIABLE_DEFINITION)
        assertThat(binVariableDeclarationNodes.size).isEqualTo(3)
        assertThat(binVariableDeclarationNodes[0].tokenValue).isEqualTo("a")
        assertThat(binVariableDeclarationNodes[1].tokenValue).isEqualTo("b")
        assertThat(binVariableDeclarationNodes[2].tokenValue).isEqualTo("c")
        val binVDeclarationNodes =
            fileNode.findChildren(MiniCGrammar.BIN_VARIABLE_DEFINITION, MiniCGrammar.BIN_FUNCTION_DEFINITION)
        assertThat(binVDeclarationNodes.size).isEqualTo(4)
        assertThat(binVDeclarationNodes[0].tokenValue).isEqualTo("a")
        assertThat(binVDeclarationNodes[1].tokenValue).isEqualTo("myFunction")
        assertThat(binVDeclarationNodes[2].tokenValue).isEqualTo("b")
        assertThat(binVDeclarationNodes[3].tokenValue).isEqualTo("c")
        assertThat(fileNode.findChildren(MiniCGrammar.MULTIPLICATIVE_EXPRESSION).size).isEqualTo(0)
    }

    @Test
    fun testFindDirectChildren() {
        val fileNode = parseString("int a = 0; void myFunction() { int b = 0*3; { int c = 0; } }")
        val declarationNodes = fileNode.findDirectChildren(MiniCGrammar.DEFINITION)
        assertThat(declarationNodes.size).isEqualTo(2)
        assertThat(declarationNodes[0].tokenValue).isEqualTo("int")
        assertThat(declarationNodes[1].tokenValue).isEqualTo("void")
        val binVDeclarationNodes = fileNode.findDirectChildren(
            MiniCGrammar.BIN_VARIABLE_DEFINITION,
            MiniCGrammar.BIN_FUNCTION_DEFINITION
        )
        assertThat(binVDeclarationNodes.size).isEqualTo(0)
    }

    @Test
    fun testFindFirstChildAndHasChildren() {
        val expr = AstNode(NodeType(), "expr", null)
        val stat = AstNode(NodeType(), "stat", null)
        val indentifierRule = NodeType()
        val identifier = AstNode(indentifierRule, "identifier", null)
        expr.addChild(stat)
        expr.addChild(identifier)
        assertThat(expr.findFirstChild(indentifierRule)).isSameAs(identifier)
        assertThat(expr.hasChildren(indentifierRule)).isTrue()
        val anotherRule = NodeType()
        assertThat(expr.findFirstChild(anotherRule)).isNull()
        assertThat(expr.hasChildren(anotherRule)).isFalse()
    }

    @Test
    fun testHasParents() {
        val exprRule = NodeType()
        val expr = AstNode(exprRule, "expr", null)
        val stat = AstNode(NodeType(), "stat", null)
        val identifier = AstNode(NodeType(), "identifier", null)
        expr.addChild(stat)
        expr.addChild(identifier)
        assertThat(identifier.hasParents(exprRule)).isTrue()
        assertThat(identifier.hasParents(NodeType())).isFalse()
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
        assertThat(a1.findChildren(b, c)).containsExactly(c1, b1, b2, b3)
        assertThat(a1.findChildren(b)).containsExactly(b1, b2, b3)
        assertThat(a1.findChildren(e)).isEmpty()
        assertThat(a1.findChildren(a)).`as`("SSLR-249").containsExactly(a1)
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