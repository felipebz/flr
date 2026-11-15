//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.grammar

import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MutableGrammarTest {
    @Test
    fun test() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = mock<CompilableGrammarRule>()
        val rootRuleKey = mock<GrammarRuleKey>()
        val rootRule = mock<CompilableGrammarRule>()
        val grammar = MutableGrammar(mapOf(Pair(ruleKey, rule), Pair(rootRuleKey, rootRule)), rootRuleKey)
        assertThat(grammar.rule(ruleKey)).isSameAs(rule)
        assertThat(grammar.rootRule).isSameAs(rootRule)
    }
}
