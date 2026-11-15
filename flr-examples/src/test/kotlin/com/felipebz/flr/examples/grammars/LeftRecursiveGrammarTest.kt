//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.examples.grammars.LeftRecursiveGrammar.Companion.eliminatedImmediateLeftRecursion
import com.felipebz.flr.examples.grammars.LeftRecursiveGrammar.Companion.eliminatedIndirectLeftRecursion
import com.felipebz.flr.examples.grammars.LeftRecursiveGrammar.Companion.immediateLeftRecursion
import com.felipebz.flr.examples.grammars.LeftRecursiveGrammar.Companion.indirectLeftRecursion
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.parser.ParseRunner
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LeftRecursiveGrammarTest {
    @Test
    fun should_detect_immediate_left_recursion() {
        val grammar = immediateLeftRecursion()
        val thrown = assertThrows<GrammarException> {
            ParseRunner(grammar.rule(LeftRecursiveGrammar.A)).parse("".toCharArray())
        }
        assertEquals(
            "Left recursion has been detected, involved rule: " + LeftRecursiveGrammar.A,
            thrown.message
        )
    }

    @Test
    fun eliminated_immediate_left_recursion() {
        val grammar = eliminatedImmediateLeftRecursion()
        assertThat(grammar.rule(LeftRecursiveGrammar.A))
            .matches("s1")
            .matches("s2")
            .matches("s1t1")
            .matches("s1t2")
            .matches("s1t1t2")
            .matches("s1t2t1")
            .matches("s2t1")
            .matches("s2t2")
            .matches("s2t1t2")
            .matches("s2t2t1")
    }

    @Test
    fun should_detect_indirect_left_recursion() {
        val grammar = indirectLeftRecursion()
        val thrown = assertThrows<GrammarException> {
            ParseRunner(grammar.rule(LeftRecursiveGrammar.A)).parse("".toCharArray())
        }
        assertEquals(
            "Left recursion has been detected, involved rule: " + LeftRecursiveGrammar.B,
            thrown.message
        )
    }

    @Test
    fun eliminated_indirect_left_recursion() {
        val grammar = eliminatedIndirectLeftRecursion()
        assertThat(grammar.rule(LeftRecursiveGrammar.A))
            .matches("s2t1")
            .matches("s2t1t2t1")
            .matches("s2t1t2t1t2t1")
            .matches("s1")
            .matches("s1t2t1")
            .matches("s1t2t1t2t1")
    }
}
