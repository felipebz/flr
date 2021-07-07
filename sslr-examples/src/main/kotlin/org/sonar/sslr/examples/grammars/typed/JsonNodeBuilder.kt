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
package org.sonar.sslr.examples.grammars.typed

import com.sonar.sslr.api.Rule
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.api.Trivia
import com.sonar.sslr.api.typed.Input
import com.sonar.sslr.api.typed.NodeBuilder
import org.sonar.sslr.examples.grammars.typed.impl.InternalSyntaxToken
import org.sonar.sslr.grammar.GrammarRuleKey

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