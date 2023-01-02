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
package com.felipebz.flr.ast

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.test.minic.MiniCParser
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CollapsibleIfVisitorTest {
    private val p = MiniCParser.create()
    private val g = p.grammar
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
        return !hasElseClause(node) && hasCollapsibleIfStatement(node)
    }

    private fun hasElseClause(node: AstNode): Boolean {
        return node.hasDirectChildren(MiniCGrammar.ELSE_CLAUSE)
    }

    private fun hasCollapsibleIfStatement(node: AstNode): Boolean {
        val statementNode = checkNotNull(checkNotNull(node.getFirstChild(MiniCGrammar.STATEMENT)).firstChild)
        return isIfStatementWithoutElse(statementNode) || isIfStatementWithoutElseInCompoundStatement(statementNode)
    }

    private fun isIfStatementWithoutElse(node: AstNode): Boolean {
        return node.`is`(MiniCGrammar.IF_STATEMENT) && !hasElseClause(node)
    }

    private fun isIfStatementWithoutElseInCompoundStatement(node: AstNode): Boolean {
        if (!node.`is`(MiniCGrammar.COMPOUND_STATEMENT) || node.numberOfChildren != 3) {
            return false
        }
        val statementNode = node.getFirstChildOrNull(MiniCGrammar.STATEMENT)
            ?: // Null check was initially forgotten, did not led to a NPE because the unit test did not cover that case yet!
            return false
        return isIfStatementWithoutElse(checkNotNull(statementNode.firstChild))
    }
}