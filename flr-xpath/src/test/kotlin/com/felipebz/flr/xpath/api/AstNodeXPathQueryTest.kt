//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.xpath.api

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AstNodeXPathQueryTest {
    @Test
    fun selectSingleNodeTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("branch/leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectSingleNode(tree)).isEqualTo(leaf)
    }

    @Test
    fun selectSingleNodeNoResultTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("branch")
        val tree = AstNode(NodeType(), "tree", null)
        assertThat(expr.selectSingleNode(tree)).isNull()
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
        assertThat(expr.selectNodes(tree).size).isEqualTo(2)
    }

    @Test
    fun selectNodesNoResultTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("//branch")
        val tree = AstNode(NodeType(), "tree", null)
        assertThat(expr.selectNodes(tree).size).isEqualTo(0)
    }

    @Test
    fun relativePathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectSingleNode(branch)).isEqualTo(leaf)
    }

    @Test
    fun parentPathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("..")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectSingleNode(branch)).isEqualTo(tree)
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
        assertThat(expr.selectSingleNode(branch1)).isEqualTo(branch2)
    }

    @Test
    fun absolutePathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("/tree")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectSingleNode(tree)).isEqualTo(tree)
    }

    @Test
    fun currentPathTest() {
        val expr = AstNodeXPathQuery.create<AstNode>(".")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectSingleNode(branch)).isEqualTo(branch)
    }

    @Test
    fun currentPathWithDescendantTest() {
        val expr = AstNodeXPathQuery.create<AstNode>("./leaf")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectSingleNode(branch)).isEqualTo(leaf)
    }

    @Test
    fun singleDocumentRoot() {
        val expr = AstNodeXPathQuery.create<AstNode>("//tree")
        val tree = AstNode(NodeType(), "tree", null)
        val branch = AstNode(NodeType(), "branch", null)
        val leaf = AstNode(NodeType(), "leaf", null)
        tree.addChild(branch)
        branch.addChild(leaf)
        assertThat(expr.selectNodes(tree).size).isEqualTo(1)
    }

    @Test
    fun relativeNamePredicate() {
        val expr = AstNodeXPathQuery.create<AstNode>(".[name() = \"tree\"]")
        val tree = AstNode(NodeType(), "tree", null)
        assertThat(expr.selectSingleNode(tree)).isEqualTo(tree)
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
        assertThat(expr.selectSingleNode(tree)).isEqualTo(tree)
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
        assertThat(expr.selectNodes(tree1).size).isEqualTo(3)
        val tree2 = AstNode(NodeType(), "tree", null)
        val branch21 = AstNode(NodeType(), "branch", null)
        tree2.addChild(branch21)
        assertThat(expr.selectNodes(tree2).size).isEqualTo(1)
    }

    internal class NodeType : AstNodeType
}
