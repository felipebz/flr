/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2026 Felipe Zorzo
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
package com.felipebz.flr.internal.matchers

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.Token
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.vm.lexerful.TokenTypeExpression
import com.felipebz.flr.parser.NonTerminalNodeBuilder
import com.felipebz.flr.parser.TerminalNodeBuilder

public class LexerfulAstCreator private constructor(
    private val tokens: List<Token>,
    private val nonTerminalNodeBuilder: NonTerminalNodeBuilder,
    private val terminalNodeBuilder: TerminalNodeBuilder
) {
    /**
     * Effective AST children projected so far and not yet given to their parent. Each node being built owns the
     * entries from the size it saw on entry, and gives them to its AST node in one call once all are projected.
     */
    private val pending = ArrayList<AstNode>()

    /**
     * Appends to [pending] the zero, one, or several effective AST children that a parse node projects to.
     */
    private fun project(node: ParseNode) {
        val ruleMatcher = node.matcher as? RuleDefinition
        if (ruleMatcher == null) {
            val terminal = projectTerminal(node)
            if (terminal != null) {
                pending.add(terminal)
            }
        } else if (ruleMatcher.isAlwaysSkipFromAst()) {
            projectChildren(node)
        } else if (ruleMatcher.isSkipIfOneChildFromAst()) {
            projectSkipIfOneChild(node, ruleMatcher)
        } else {
            pending.add(projectNonTerminal(node, ruleMatcher))
        }
    }

    private fun projectChildren(node: ParseNode) {
        for (child in node.children) {
            project(child)
        }
    }

    private fun projectSkipIfOneChild(node: ParseNode, ruleMatcher: RuleDefinition) {
        val start = pending.size
        projectChildren(node)
        val count = pending.size - start
        if (count == 1) {
            // the only child stays in pending, in place of this node
            return
        }
        val astNode = buildNonTerminal(node, ruleMatcher)
        if (count > 1) {
            takeChildren(astNode, start)
        }
        pending.add(astNode)
    }

    private fun projectNonTerminal(node: ParseNode, ruleMatcher: RuleDefinition): AstNode {
        val astNode = buildNonTerminal(node, ruleMatcher)
        val start = pending.size
        projectChildren(node)
        takeChildren(astNode, start)
        return astNode
    }

    private fun buildNonTerminal(node: ParseNode, ruleMatcher: RuleDefinition): AstNode {
        val token = if (node.startIndex < tokens.size) tokens[node.startIndex] else null
        return nonTerminalNodeBuilder.build(ruleMatcher, ruleMatcher.getName(), token)
    }

    /**
     * Gives the entries of [pending] from [start] to [parent], then removes them from [pending].
     */
    private fun takeChildren(parent: AstNode, start: Int) {
        parent.setChildren(pending, start, pending.size)
        for (i in pending.size - 1 downTo start) {
            pending.removeAt(i)
        }
    }

    private fun projectTerminal(node: ParseNode): AstNode? {
        val token = tokens[node.startIndex]
        // For compatibility with SSLR < 1.19, TokenType should be checked only for TokenTypeExpression:
        if (node.matcher is TokenTypeExpression && token.type.hasToBeSkippedFromAst(null)) {
            return null
        }
        return terminalNodeBuilder.build(token)
    }

    public companion object {
        @JvmStatic
        public fun create(
            node: ParseNode,
            tokens: List<Token>,
            nonTerminalNodeBuilder: NonTerminalNodeBuilder,
            terminalNodeBuilder: TerminalNodeBuilder
        ): AstNode {
            val creator = LexerfulAstCreator(tokens, nonTerminalNodeBuilder, terminalNodeBuilder)
            // The root is always a node, whatever its skipping policy
            val ruleMatcher = node.matcher as? RuleDefinition
            val astNode = if (ruleMatcher != null) {
                creator.projectNonTerminal(node, ruleMatcher)
            } else {
                checkNotNull(creator.projectTerminal(node))
            }
            // Unwrap AstNodeType for root node:
            astNode.hasToBeSkippedFromAst()
            return astNode
        }
    }
}
