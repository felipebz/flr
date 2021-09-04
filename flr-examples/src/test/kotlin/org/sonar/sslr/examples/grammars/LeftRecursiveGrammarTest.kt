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
package org.sonar.sslr.examples.grammars

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.sonar.sslr.examples.grammars.LeftRecursiveGrammar.Companion.eliminatedImmediateLeftRecursion
import org.sonar.sslr.examples.grammars.LeftRecursiveGrammar.Companion.eliminatedIndirectLeftRecursion
import org.sonar.sslr.examples.grammars.LeftRecursiveGrammar.Companion.immediateLeftRecursion
import org.sonar.sslr.examples.grammars.LeftRecursiveGrammar.Companion.indirectLeftRecursion
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.parser.ParseRunner
import org.sonar.sslr.tests.Assertions.assertThat

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