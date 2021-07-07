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
package org.sonar.sslr.internal.matchers

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.vm.lexerful.TokenTypeExpression

public class LexerfulAstCreator private constructor(private val tokens: List<Token>) {
    private fun visit(node: ParseNode): AstNode? {
        return if (node.matcher is RuleDefinition) {
            visitNonTerminal(node)
        } else {
            visitTerminal(node)
        }
    }

    private fun visitNonTerminal(node: ParseNode): AstNode {
        val astNodes = mutableListOf<AstNode>()
        for (child in node.children) {
            val astNode = visit(child)
            when {
                astNode == null -> {
                    // skip
                }
                astNode.hasToBeSkippedFromAst() -> {
                    astNodes.addAll(astNode.children)
                }
                else -> {
                    astNodes.add(astNode)
                }
            }
        }
        val ruleMatcher = node.matcher as RuleDefinition
        val token = if (node.startIndex < tokens.size) tokens[node.startIndex] else null
        val astNode = AstNode(ruleMatcher, ruleMatcher.getName(), token)
        for (child in astNodes) {
            astNode.addChild(child)
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
        val astNode = AstNode(token)
        astNode.fromIndex = node.startIndex
        astNode.toIndex = node.endIndex
        return astNode
    }

    public companion object {
        @JvmStatic
        public fun create(node: ParseNode, tokens: List<Token>): AstNode {
            val astNode = checkNotNull(LexerfulAstCreator(tokens).visit(node))
            // Unwrap AstNodeType for root node:
            astNode.hasToBeSkippedFromAst()
            return astNode
        }
    }
}