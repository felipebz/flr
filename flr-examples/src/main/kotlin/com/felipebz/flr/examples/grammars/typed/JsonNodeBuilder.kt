//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed

import com.felipebz.flr.api.Rule
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.api.Trivia
import com.felipebz.flr.api.typed.Input
import com.felipebz.flr.api.typed.NodeBuilder
import com.felipebz.flr.examples.grammars.typed.impl.InternalSyntaxToken
import com.felipebz.flr.grammar.GrammarRuleKey

public class JsonNodeBuilder : NodeBuilder {
    /**
     * This methods is called for every rule defined in JsonLexer (i.e. TRUE, NUMBER, etc.).
     * Despite the fact that these rules are supposed to define tokens (as [JsonNodeBuilder.createTerminal] method does), this method is called due to whitespaces.
     * The whitespace token is dropped, and only the first one is returned.
     */
    override fun createNonTerminal(
        ruleKey: GrammarRuleKey,
        rule: Rule,
        children: List<Any?>,
        startIndex: Int,
        endIndex: Int
    ): Any {
        for (child in children) {
            if (child is InternalSyntaxToken) {
                return child
            }
        }
        throw IllegalStateException()
    }

    override fun createTerminal(
        input: Input,
        startIndex: Int,
        endIndex: Int,
        trivias: List<Trivia>,
        type: TokenType?
    ): Any {
        val value = input.substring(startIndex, endIndex)
        return InternalSyntaxToken(value)
    }
}
