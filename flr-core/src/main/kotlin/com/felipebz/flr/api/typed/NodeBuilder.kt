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

import com.felipebz.flr.api.Rule
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.api.Trivia
import com.felipebz.flr.grammar.GrammarRuleKey

/**
 * @since 1.21
 */
public interface NodeBuilder {
    public fun createNonTerminal(
        ruleKey: GrammarRuleKey,
        rule: Rule,
        children: List<Any?>,
        startIndex: Int,
        endIndex: Int
    ): Any

    public fun createTerminal(input: Input, startIndex: Int, endIndex: Int, trivias: List<Trivia>, type: TokenType?): Any
}