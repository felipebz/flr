//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.grammar

import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.parser.LexerlessGrammar

public class MutableGrammar(
    private val rules: Map<GrammarRuleKey, CompilableGrammarRule>,
    private val rootRuleKey: GrammarRuleKey?
) : LexerlessGrammar() {
    override fun rule(ruleKey: GrammarRuleKey): Rule {
        return requireNotNull(rules[ruleKey])
    }

    override val rootRule: Rule
        get() = rule(checkNotNull(rootRuleKey))
}
