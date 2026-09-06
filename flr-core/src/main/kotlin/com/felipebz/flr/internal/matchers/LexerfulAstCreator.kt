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
     * Projects a parse node to zero, one, or several effective AST children.
     *
     * The common one-child result is represented by the child itself. A list is
     * created only when an always-skipped node produces several children.
     */
    private fun project(node: ParseNode, forceNode: Boolean = false): Any? {
        val ruleMatcher = node.matcher as? RuleDefinition
        if (!forceNode && ruleMatcher != null) {
            if (ruleMatcher.isAlwaysSkipFromAst()) {
                return projectAlwaysSkipped(node)
            }
            if (ruleMatcher.isSkipIfOneChildFromAst()) {
                return projectSkipIfOneChild(node, ruleMatcher)
            }
        }

        return if (ruleMatcher != null) {
            projectNonTerminal(node, ruleMatcher)
        } else {
            projectTerminal(node)
        }
    }

    private fun projectAlwaysSkipped(node: ParseNode): Any? {
        var firstChild: AstNode? = null
        var multipleChildren: ArrayList<AstNode>? = null

        for (child in node.children) {
            val projection = project(child)
            if (projection is AstNode) {
                if (firstChild == null) {
                    firstChild = projection
                } else {
                    if (multipleChildren == null) {
                        multipleChildren = ArrayList()
                        multipleChildren.add(firstChild)
                    }
                    multipleChildren.add(projection)
                }
            } else if (projection is List<*>) {
                for (projectedChild in projection) {
                    val astChild = projectedChild as AstNode
                    if (firstChild == null) {
                        firstChild = astChild
                    } else {
                        if (multipleChildren == null) {
                            multipleChildren = ArrayList()
                            multipleChildren.add(firstChild)
                        }
                        multipleChildren.add(astChild)
                    }
                }
            }
        }

        return multipleChildren ?: firstChild
    }

    private fun projectSkipIfOneChild(node: ParseNode, ruleMatcher: RuleDefinition): Any {
        var firstChild: AstNode? = null
        var retainedNode: AstNode? = null

        for (child in node.children) {
            val projection = project(child)
            if (projection is AstNode) {
                if (retainedNode != null) {
                    addChild(retainedNode, projection)
                } else if (firstChild == null) {
                    firstChild = projection
                } else {
                    retainedNode = buildNonTerminal(node, ruleMatcher)
                    addChild(retainedNode, checkNotNull(firstChild))
                    addChild(retainedNode, projection)
                }
            } else if (projection is List<*>) {
                for (projectedChild in projection) {
                    val astChild = projectedChild as AstNode
                    if (retainedNode != null) {
                        addChild(retainedNode, astChild)
                    } else if (firstChild == null) {
                        firstChild = astChild
                    } else {
                        retainedNode = buildNonTerminal(node, ruleMatcher)
                        addChild(retainedNode, checkNotNull(firstChild))
                        addChild(retainedNode, astChild)
                    }
                }
            }
        }

        if (retainedNode == null) {
            if (firstChild != null) {
                return firstChild
            }
            return buildNonTerminal(node, ruleMatcher)
        }

        retainedNode.fromIndex = node.startIndex
        retainedNode.toIndex = node.endIndex
        return retainedNode
    }

    private fun projectNonTerminal(node: ParseNode, ruleMatcher: RuleDefinition): AstNode {
        val astNode = buildNonTerminal(node, ruleMatcher)
        for (child in node.children) {
            attachProjection(astNode, project(child))
        }
        astNode.fromIndex = node.startIndex
        astNode.toIndex = node.endIndex
        return astNode
    }

    private fun buildNonTerminal(node: ParseNode, ruleMatcher: RuleDefinition): AstNode {
        val token = if (node.startIndex < tokens.size) tokens[node.startIndex] else null
        return nonTerminalNodeBuilder.build(ruleMatcher, ruleMatcher.getName(), token)
    }

    private fun attachProjection(parent: AstNode, projection: Any?) {
        when (projection) {
            is AstNode -> addChild(parent, projection)
            is List<*> -> {
                for (child in projection) {
                    addChild(parent, child as AstNode)
                }
            }
        }
    }

    private fun addChild(parent: AstNode, child: AstNode) {
        parent.addChild(child)
    }

    private fun projectTerminal(node: ParseNode): AstNode? {
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
            val astNode = checkNotNull(
                LexerfulAstCreator(tokens, nonTerminalNodeBuilder, terminalNodeBuilder).project(node, forceNode = true)
                    as? AstNode
            )
            // Unwrap AstNodeType for root node:
            astNode.hasToBeSkippedFromAst()
            return astNode
        }
    }
}
