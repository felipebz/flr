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
package org.sonar.sslr.ast

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.test.minic.MiniCGrammar
import com.sonar.sslr.test.minic.MiniCParser
import org.fest.assertions.Assertions.assertThat
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