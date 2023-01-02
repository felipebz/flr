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
package com.felipebz.flr.impl.typed

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.api.Trivia
import com.felipebz.flr.api.Trivia.TriviaKind
import com.felipebz.flr.api.typed.Input
import com.felipebz.flr.api.typed.NodeBuilder
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.matchers.ParseNode
import com.felipebz.flr.internal.vm.TokenExpression
import com.felipebz.flr.internal.vm.TriviaExpression
import java.util.*

public class SyntaxTreeCreator<T>(
    private val treeFactory: Any,
    private val mapping: GrammarBuilderInterceptor<*>,
    private val nodeBuilder: NodeBuilder
) {
    private val tokenBuilder = Token.builder()
    private val trivias = mutableListOf<Trivia>()
    private lateinit var input: Input

    public fun create(node: ParseNode, input: Input): T? {
        this.input = input
        trivias.clear()
        return visit(node) as T?
    }

    private fun visit(node: ParseNode): Any? {
        return if (node.matcher is MutableParsingRule) {
            visitNonTerminal(node)
        } else {
            visitTerminal(node)
        }
    }

    private fun visitNonTerminal(node: ParseNode): Any? {
        val rule = node.matcher as MutableParsingRule
        val ruleKey = rule.ruleKey
        val children = node.children
        return if (mapping.hasMethodForRuleKey(ruleKey)) {
            // TODO Drop useless intermediate nodes
            check(children.size == 1)
            visit(children[0])
        } else if (mapping.isOptionalRule(ruleKey)) {
            check(children.size <= 1)
            if (children.isEmpty()) {
                Optional.empty()
            } else {
                Optional.of(visit(children[0]) as Any)
            }
        } else {
            val method = mapping.actionForRuleKey(ruleKey)
            val convertedChildren = children.map { visit(it) }
            if (mapping.isOneOrMoreRule(ruleKey)) {
                convertedChildren
            } else if (mapping.isZeroOrMoreRule(ruleKey)) {
                if (convertedChildren.isEmpty()) Optional.empty() else Optional.of(convertedChildren)
            } else if (method == null) {
                nodeBuilder.createNonTerminal(
                    ruleKey,
                    rule,
                    convertedChildren,
                    node.startIndex,
                    node.endIndex
                )
            } else {
                ReflectionUtils.invokeMethod(method, treeFactory, *convertedChildren.toTypedArray())
            }
        }
    }

    private fun visitTerminal(node: ParseNode): Any? {
        var type: TokenType? = null
        if (node.matcher is TriviaExpression) {
            val ruleMatcher = node.matcher
            return when (ruleMatcher.getTriviaKind()) {
                 TriviaKind.SKIPPED_TEXT -> {
                    null
                }
                TriviaKind.COMMENT -> {
                    addComment(node)
                    null
                }
            }
        } else if (node.matcher is TokenExpression) {
            val ruleMatcher = node.matcher
            type = ruleMatcher.getTokenType()
            if (GenericTokenType.COMMENT == ruleMatcher.getTokenType()) {
                addComment(node)
                return null
            }
        }
        val result = nodeBuilder.createTerminal(input, node.startIndex, node.endIndex, trivias, type)
        trivias.clear()
        return result
    }

    private fun addComment(node: ParseNode) {
        tokenBuilder.setGeneratedCode(false)
        val lineAndColumn = input.lineAndColumnAt(node.startIndex)
        tokenBuilder.setLine(lineAndColumn[0])
        tokenBuilder.setColumn(lineAndColumn[1] - 1)
        val value = input.substring(node.startIndex, node.endIndex)
        tokenBuilder.setValueAndOriginalValue(value)
        tokenBuilder.setTrivia(emptyList())
        tokenBuilder.setType(GenericTokenType.COMMENT)
        trivias.add(Trivia.createComment(tokenBuilder.build()))
    }
}