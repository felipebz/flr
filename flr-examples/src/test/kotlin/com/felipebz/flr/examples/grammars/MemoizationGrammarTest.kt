//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.examples.grammars.MemoizationGrammar.Companion.requiresNegativeMemoization
import com.felipebz.flr.examples.grammars.MemoizationGrammar.Companion.requiresPositiveMemoization
import com.felipebz.flr.examples.grammars.MemoizationGrammar.Companion.requiresPositiveMemoizationOnMoreThanJustLastRule
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class MemoizationGrammarTest {
    @Test
    fun should_be_slow_to_fail_to_parse_gramar_requiring_negative_memoization() {
        val grammar = requiresNegativeMemoization()
        assertThat(grammar.rule(MemoizationGrammar.A))
            .notMatches("aaaaaaaaaaaaaaa") // Requires time T
            .notMatches("aaaaaaaaaaaaaaaa") // Requires time 2*T
            .notMatches("aaaaaaaaaaaaaaaaa") // Requires time 4*T, etc.
    }

    @Test
    fun should_be_fast_on_grammar_requiring_positive_memoization() {
        val grammar = requiresPositiveMemoization()
        assertThat(grammar.rule(MemoizationGrammar.A))
            .matches("((((((((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time T
            .matches("((((((((((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time ~T
            .matches("((((((((((((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time ~T, etc.
    }

    @Test
    fun should_be_slow_on_grammar_requiring_positive_memoization_on_more_than_just_the_last_rule() {
        val grammar = requiresPositiveMemoizationOnMoreThanJustLastRule()
        assertThat(grammar.rule(MemoizationGrammar.A))
            .matches("(((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time T
            .matches("(((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time 4*T
            .matches("(((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time 16*T, etc.
    }
}
