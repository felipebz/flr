/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.examples.grammars

import org.junit.Test
import org.sonar.sslr.examples.grammars.MemoizationGrammar.Companion.requiresNegativeMemoization
import org.sonar.sslr.examples.grammars.MemoizationGrammar.Companion.requiresPositiveMemoization
import org.sonar.sslr.examples.grammars.MemoizationGrammar.Companion.requiresPositiveMemoizationOnMoreThanJustLastRule
import org.sonar.sslr.tests.Assertions

class MemoizationGrammarTest {
    @Test
    fun should_be_slow_to_fail_to_parse_gramar_requiring_negative_memoization() {
        val grammar = requiresNegativeMemoization()
        Assertions.assertThat(grammar.rule(MemoizationGrammar.A))
            .notMatches("aaaaaaaaaaaaaaa") // Requires time T
            .notMatches("aaaaaaaaaaaaaaaa") // Requires time 2*T
            .notMatches("aaaaaaaaaaaaaaaaa") // Requires time 4*T, etc.
    }

    @Test
    fun should_be_fast_on_grammar_requiring_positive_memoization() {
        val grammar = requiresPositiveMemoization()
        Assertions.assertThat(grammar.rule(MemoizationGrammar.A))
            .matches("((((((((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time T
            .matches("((((((((((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time ~T
            .matches("((((((((((((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time ~T, etc.
    }

    @Test
    fun should_be_slow_on_grammar_requiring_positive_memoization_on_more_than_just_the_last_rule() {
        val grammar = requiresPositiveMemoizationOnMoreThanJustLastRule()
        Assertions.assertThat(grammar.rule(MemoizationGrammar.A))
            .matches("(((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time T
            .matches("(((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time 4*T
            .matches("(((((((((((((((((((b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b)b") // Requires time 16*T, etc.
    }
}