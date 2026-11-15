//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api.typed

import com.felipebz.flr.api.*
import com.felipebz.flr.grammar.GrammarRuleKey

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
        @Suppress("UNCHECKED_CAST")
        astNode.setChildren(children as List<AstNode?>)
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
