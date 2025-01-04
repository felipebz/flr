/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
    private fun visit(node: ParseNode): AstNode? {
        return if (node.matcher is RuleDefinition) {
            visitNonTerminal(node)
        } else {
            visitTerminal(node)
        }
    }

    private fun visitNonTerminal(node: ParseNode): AstNode {
        val ruleMatcher = node.matcher as RuleDefinition
        val token = if (node.startIndex < tokens.size) tokens[node.startIndex] else null
        val astNode = nonTerminalNodeBuilder.build(ruleMatcher, ruleMatcher.getName(), token)
        for (child in node.children) {
            val internalAstNode = visit(child)
            when {
                internalAstNode == null -> {
                    // skip
                }
                internalAstNode.hasToBeSkippedFromAst() -> {
                    internalAstNode.children.forEach { astNode.addChild(it)  }
                }
                else -> {
                    astNode.addChild(internalAstNode)
                }
            }
        }
        astNode.fromIndex = node.startIndex
        astNode.toIndex = node.endIndex
        return astNode
    }

    private fun visitTerminal(node: ParseNode): AstNode? {
        val token = tokens[node.startIndex]
        // For compatibility with SSLR < 1.19, TokenType should be checked only for TokenTypeExpression:
        if (node.matcher is TokenTypeExpression && token.type.hasToBeSkippedFromAst(null)) {
            return null
        }
        val astNode = terminalNodeBuilder.build(token)
        astNode.fromIndex = node.startIndex
        astNode.toIndex = node.endIndex
        return astNode
    }

    public companion object {
        @JvmStatic
        public fun create(
            node: ParseNode,
            tokens: List<Token>,
            nonTerminalNodeBuilder: NonTerminalNodeBuilder,
            terminalNodeBuilder: TerminalNodeBuilder
        ): AstNode {
            val astNode = checkNotNull(LexerfulAstCreator(tokens, nonTerminalNodeBuilder, terminalNodeBuilder).visit(node))
            // Unwrap AstNodeType for root node:
            astNode.hasToBeSkippedFromAst()
            return astNode
        }
    }
}
