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
package com.sonar.sslr.xpath.api

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeType
import org.fest.assertions.Assertions
import org.junit.Test

class AstNodeXPathQueryTest {
    @Test
    fun selectSingleNodeTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("branch/leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(tree)).isEqualTo(leaf)
    }

    @Test
    fun selectSingleNodeNoResultTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("branch")
        val tree = AstNode(NodeType(), "tree", null)
        Assertions.assertThat(expr.selectSingleNode(tree)).isNull()
    }

    @Test
    fun selectNodesTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("//leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf1 = AstNode(NodeType(), "leaf", null)
        val leaf2 = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf1)
        branch.addChild(leaf2)
        Assertions.assertThat(expr.selectNodes(tree).size).isEqualTo(2)
    }

    @Test
    fun selectNodesNoResultTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("//branch")
        val tree = AstNode(NodeType(), "tree", null)
        Assertions.assertThat(expr.selectNodes(tree).size).isEqualTo(0)
    }

    @Test
    fun relativePathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(branch)).isEqualTo(leaf)
    }

    @Test
    fun parentPathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("..")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(branch)).isEqualTo(tree)
    }

    @Test
    fun parentAndDescendingPathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("../branch2")
        val tree = AstNode(NodeType(), "tree", null)
        val branch1 = AstNode(NodeType(), "branch1", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        val branch2 = AstNode(NodeType(), "branch2", null)
        tree.addChild(branch1)
        tree.addChild(branch2)
        branch1.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(branch1)).isEqualTo(branch2)
    }

    @Test
    fun absolutePathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("/tree")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(tree)).isEqualTo(tree)
    }

    @Test
    fun currentPathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>(".")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(branch)).isEqualTo(branch)
    }

    @Test
    fun currentPathWithDescendantTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("./leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectSingleNode(branch)).isEqualTo(leaf)
    }

    @Test
    fun singleDocumentRoot() {
        val expr = AstNodeXPathQuery.create<AstNode>("//tree")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        Assertions.assertThat(expr.selectNodes(tree).size).isEqualTo(1)
    }

    @Test
    fun relativeNamePredicate() {
        val expr = AstNodeXPathQuery.create<AstNode>(".[name() = \"tree\"]")
        val tree = AstNode(NodeType(), "tree", null)
        Assertions.assertThat(expr.selectSingleNode(tree)).isEqualTo(tree)
    }

    @Test
    fun relativeCountPredicate() {
        val expr = AstNodeXPathQuery.create<AstNode>(".[count(*) = 3]")
        val tree = AstNode(NodeType(), "tree", null)
        val branch1 = AstNode(NodeType(), "branch1", null)
        val branch2 = AstNode(NodeType(), "branch2", null)
        val branch3 = AstNode(NodeType(), "branch3", null)
        tree.addChild(branch1)
        tree.addChild(branch2)
        tree.addChild(branch3)
        Assertions.assertThat(expr.selectSingleNode(tree)).isEqualTo(tree)
    }

    @Test
    fun noCacheTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("//branch")
        val tree1 = AstNode(NodeType(), "tree", null)
        val branch11 = AstNode(NodeType(), "branch", null)
        val branch12 = AstNode(NodeType(), "branch", null)
        val branch13 = AstNode(NodeType(), "branch", null)
        tree1.addChild(branch11)
        tree1.addChild(branch12)
        tree1.addChild(branch13)
        Assertions.assertThat(expr.selectNodes(tree1).size).isEqualTo(3)
        val tree2 = AstNode(NodeType(), "tree", null)
        val branch21 = AstNode(NodeType(), "branch", null)
        tree2.addChild(branch21)
        Assertions.assertThat(expr.selectNodes(tree2).size).isEqualTo(1)
    }

    internal class NodeType : AstNodeType
}