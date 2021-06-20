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
package org.sonar.sslr.internal.matchers

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.vm.lexerful.TokenTypeExpression

class LexerfulAstCreator private constructor(private val tokens: List<Token>) {
    private fun visit(node: ParseNode): AstNode? {
        return if (node.getMatcher() is RuleDefinition) {
            visitNonTerminal(node)
        } else {
            visitTerminal(node)
        }
    }

    private fun visitNonTerminal(node: ParseNode): AstNode {
        val astNodes = mutableListOf<AstNode>()
        for (child in node.getChildren()) {
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
        val ruleMatcher = node.getMatcher() as RuleDefinition
        val token = if (node.getStartIndex() < tokens.size) tokens[node.getStartIndex()] else null
        val astNode = AstNode(ruleMatcher, ruleMatcher.getName(), token)
        for (child in astNodes) {
            astNode.addChild(child)
        }
        astNode.fromIndex = node.getStartIndex()
        astNode.toIndex = node.getEndIndex()
        return astNode
    }

    private fun visitTerminal(node: ParseNode): AstNode? {
        val token = tokens[node.getStartIndex()]
        // For compatibility with SSLR < 1.19, TokenType should be checked only for TokenTypeExpression:
        if (node.getMatcher() is TokenTypeExpression && token.type.hasToBeSkippedFromAst(null)) {
            return null
        }
        val astNode = AstNode(token)
        astNode.fromIndex = node.getStartIndex()
        astNode.toIndex = node.getEndIndex()
        return astNode
    }

    companion object {
        @JvmStatic
        fun create(node: ParseNode, tokens: List<Token>): AstNode {
            val astNode = checkNotNull(LexerfulAstCreator(tokens).visit(node))
            // Unwrap AstNodeType for root node:
            astNode.hasToBeSkippedFromAst()
            return astNode
        }
    }
}