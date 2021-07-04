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
package com.sonar.sslr.api.typed

import com.sonar.sslr.api.AstNode
import org.sonar.sslr.grammar.GrammarRuleKey

/**
 * @since 1.21
 */
interface GrammarBuilder<T> {
    fun <U> nonterminal(): NonterminalBuilder<U>
    fun <U> nonterminal(ruleKey: GrammarRuleKey): NonterminalBuilder<U>
    fun <U> firstOf(vararg methods: U?): U?
    fun <U> optional(method: U): Optional<U>
    fun <U> oneOrMore(method: U): List<U>?
    fun <U> zeroOrMore(method: U): Optional<List<U>>?
    fun invokeRule(ruleKey: GrammarRuleKey): AstNode?
    fun token(ruleKey: GrammarRuleKey): T
}