//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api.typed

import com.felipebz.flr.api.Rule
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.api.Trivia
import com.felipebz.flr.grammar.GrammarRuleKey

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
