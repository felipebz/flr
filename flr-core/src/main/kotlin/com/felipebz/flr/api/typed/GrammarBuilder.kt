//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api.typed

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.grammar.GrammarRuleKey
import java.util.*

public interface GrammarBuilder<T> {
    public fun <U> nonterminal(): NonterminalBuilder<U>
    public fun <U> nonterminal(ruleKey: GrammarRuleKey): NonterminalBuilder<U>
    public fun <U> firstOf(vararg methods: U?): U?
    public fun <U> optional(method: U): Optional<U>?
    public fun <U> oneOrMore(method: U): List<U>?
    public fun <U> zeroOrMore(method: U): Optional<List<U>>?
    public fun invokeRule(ruleKey: GrammarRuleKey): AstNode?
    public fun token(ruleKey: GrammarRuleKey): T
}
