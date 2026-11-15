//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.ast

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.test.minic.MiniCParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CollapsibleIfSelectTest {
    private val p = MiniCParser.create()

    @Test
    fun test() {
        val fileNode = p.parse(File("src/test/resources/queries/collapsible_if.mc"))
        val ifStatements: List<AstNode> = fileNode.getDescendants(MiniCGrammar.IF_STATEMENT)
        val violations: MutableSet<Int?> = HashSet()
        for (node in ifStatements) {
            if (visit(node)) {
                violations.add(node.tokenLine)
            }
        }
        assertThat(violations).containsOnly(7, 16)
    }

    private fun visit(node: AstNode): Boolean {
        return hasNoElseClause(node) && (hasIfStatementWithoutElse(listOf(node)) || hasIfStatementWithoutElseInCompoundStatement(node))
    }

    private fun hasNoElseClause(node: AstNode): Boolean {
        return node.getChildren(MiniCGrammar.ELSE_CLAUSE).isEmpty()
    }

    private fun hasIfStatementWithoutElseInCompoundStatement(node: AstNode): Boolean {
        val statements = node
            .getChildren(MiniCGrammar.STATEMENT)
            .flatMap { it.getChildren(MiniCGrammar.COMPOUND_STATEMENT) }
        return (statements.flatMap { it.children }.size == 3
                && hasIfStatementWithoutElse(statements))
    }

    private fun hasIfStatementWithoutElse(nodes: List<AstNode>): Boolean {
        val statements = nodes.flatMap { it.getChildren(MiniCGrammar.STATEMENT) }.flatMap { it.getChildren(MiniCGrammar.IF_STATEMENT) }
        return statements.isNotEmpty() && statements.all { hasNoElseClause(it) }
    }
}
