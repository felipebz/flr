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
package com.felipebz.flr.api.typed

import com.felipebz.flr.api.*
import com.felipebz.flr.grammar.GrammarRuleKey

/**
 * @since 1.21
 */
internal class AstNodeBuilder : NodeBuilder {
    override fun createNonTerminal(
        ruleKey: GrammarRuleKey,
        rule: Rule,
        children: List<Any?>,
        startIndex: Int,
        endIndex: Int
    ): AstNode {
        var token: Token? = null
        for (child in children) {
            if (child is AstNode && child.hasToken()) {
                token = child.token
                break
            }
        }
        val astNode = AstNode(rule, ruleKey.toString(), token)
        for (child in children) {
            astNode.addChild(child as AstNode?)
        }
        astNode.fromIndex = startIndex
        astNode.toIndex = endIndex
        return astNode
    }

    override fun createTerminal(
        input: Input,
        startIndex: Int,
        endIndex: Int,
        trivias: List<Trivia>,
        type: TokenType?
    ): AstNode {
        val lineAndColumn = input.lineAndColumnAt(startIndex)
        val token: Token = Token.builder()
            .setType(type ?: UndefinedTokenType)
            .setLine(lineAndColumn[0])
            .setColumn(lineAndColumn[1] - 1)
            .setValueAndOriginalValue(input.substring(startIndex, endIndex))
            .setGeneratedCode(false)
            .setTrivia(trivias)
            .build()
        val astNode = AstNode(token)
        astNode.fromIndex = startIndex
        astNode.toIndex = endIndex
        return astNode
    }

    private object UndefinedTokenType : TokenType {
        override val name: String
            get() = "TOKEN"
        override val value: String
            get() = name

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }

        override fun toString(): String {
            return UndefinedTokenType::class.java.simpleName
        }
    }
}